const fs = require('fs');
const http = require('http');

const chromePort = Number(process.env.CHROME_DEBUG_PORT || 9225);
const appUrl = process.env.APP_URL || 'http://127.0.0.1:5173';
const screenshotPath = process.env.SCREENSHOT_PATH || 'dashboard-cctv-check.png';

function getJson(url) {
  return new Promise((resolve, reject) => {
    http.get(url, response => {
      let body = '';
      response.on('data', chunk => {
        body += chunk;
      });
      response.on('end', () => {
        try {
          resolve(JSON.parse(body));
        } catch (error) {
          reject(error);
        }
      });
    }).on('error', reject);
  });
}

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function connectCdp() {
  const tabs = await getJson(`http://127.0.0.1:${chromePort}/json`);
  const tab = tabs.find(item => item.type === 'page') || tabs[0];
  if (!tab?.webSocketDebuggerUrl) {
    throw new Error('No Chrome page target with a DevTools websocket URL was found.');
  }

  const ws = new WebSocket(tab.webSocketDebuggerUrl);
  let id = 0;
  const pending = new Map();

  ws.addEventListener('message', event => {
    const message = JSON.parse(event.data);
    if (!message.id || !pending.has(message.id)) return;
    const { resolve, reject } = pending.get(message.id);
    pending.delete(message.id);
    if (message.error) reject(new Error(JSON.stringify(message.error)));
    else resolve(message.result);
  });

  await new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error('Timed out opening Chrome DevTools websocket.')), 10000);
    ws.addEventListener('open', () => {
      clearTimeout(timer);
      resolve();
    }, { once: true });
    ws.addEventListener('error', reject, { once: true });
  });

  function send(method, params = {}) {
    return new Promise((resolve, reject) => {
      const messageId = ++id;
      pending.set(messageId, { resolve, reject });
      ws.send(JSON.stringify({ id: messageId, method, params }));
      setTimeout(() => {
        if (!pending.has(messageId)) return;
        pending.delete(messageId);
        reject(new Error(`Timed out waiting for ${method}.`));
      }, 15000);
    });
  }

  return { ws, send };
}

async function main() {
  const { ws, send } = await connectCdp();
  try {
    await send('Page.enable');
    await send('Runtime.enable');
    await send('Emulation.setDeviceMetricsOverride', {
      width: 1440,
      height: 1000,
      deviceScaleFactor: 1,
      mobile: false,
    });
    await send('Page.navigate', { url: appUrl });
    await delay(1500);

    const loginExpression = `
      (() => {
        Array.from(document.querySelectorAll('button')).find(button => button.textContent.includes('관리자'))?.click();
        const setValue = (input, value) => {
          const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
          setter.call(input, value);
          input.dispatchEvent(new Event('input', { bubbles: true }));
        };
        setValue(document.querySelector('input[type=text]'), 'admin');
        setValue(document.querySelector('input[type=password]'), 'admin');
        Array.from(document.querySelectorAll('button')).find(button => button.textContent.trim() === '로그인')?.click();
      })()
    `;
  await send('Runtime.evaluate', { expression: loginExpression });
  await delay(2500);

  await send('Runtime.evaluate', {
    expression: `
      Array.from(document.querySelectorAll('button'))
        .find(button => button.textContent.includes('모니터링'))
        ?.click();
    `,
  });
  await delay(1500);

  const inspection = await send('Runtime.evaluate', {
      expression: `
        (() => ({
          bodyText: document.body.innerText,
          imageSources: Array.from(document.images).map(image => image.src),
          cctvButtons: Array.from(document.querySelectorAll('button'))
            .map(button => button.innerText)
            .filter(text => text.includes('CCTV')),
          offlineVisible: document.body.innerText.includes('OFFLINE'),
          liveVisible: document.body.innerText.includes('LIVE'),
          streamImages: Array.from(document.images).filter(image => image.src.includes('/stream/camera-')).length
        }))()
      `,
      returnByValue: true,
    });

    const screenshot = await send('Page.captureScreenshot', {
      format: 'png',
      captureBeyondViewport: false,
    });
    fs.writeFileSync(screenshotPath, Buffer.from(screenshot.data, 'base64'));

    const value = inspection.result.value;
    const proof = {
      url: appUrl,
      screenshotPath,
      streamImages: value.streamImages,
      offlineVisible: value.offlineVisible,
      liveVisible: value.liveVisible,
      cctvButtons: value.cctvButtons,
      imageSources: value.imageSources,
    };
    console.log(JSON.stringify(proof, null, 2));

    if (value.cctvButtons.length < 4) {
      throw new Error(`Expected at least 4 CCTV cards, got ${value.cctvButtons.length}.`);
    }
    if (!value.liveVisible) {
      throw new Error('LIVE status was not visible on the dashboard.');
    }
    if (value.imageSources.some(source => source.includes('hospital_hallway'))) {
      throw new Error('Legacy hospital hallway image is still rendered.');
    }
  } finally {
    ws.close();
  }
}

main().catch(error => {
  console.error(error);
  process.exit(1);
});
