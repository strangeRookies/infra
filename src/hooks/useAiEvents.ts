import { useEffect, useState } from 'react';
import { z } from 'zod';
import { SimpleStompClient } from '../shared/utils/stomp';

const unknownRecordSchema = z.record(z.string(), z.unknown());
const aiEventSchema = z.object({
  camera_id: z.string(),
  frame_idx: z.number().default(0),
  timestamp: z.number(),
  event_type: z.string(),
  score: z.number().default(0),
  confidence: z.number().default(0),
  boxes: z.array(unknownRecordSchema).default([]),
  bbox: z.unknown().nullable().optional(),
  threshold: z.number().default(0),
  track_id: z.union([z.string(), z.number()]).nullable().optional(),
  severity: z.string().default('HIGH'),
});

export interface AiEvent {
  readonly camera_id: string;
  readonly frame_idx: number;
  readonly timestamp: number;
  readonly event_type: string;
  readonly score: number;
  readonly confidence: number;
  readonly boxes: readonly Record<string, unknown>[];
  readonly bbox: unknown;
  readonly threshold: number;
  readonly track_id: string | null;
  readonly severity: string;
}

interface UseAiEventsOptions {
  readonly url?: string;
  readonly enabled?: boolean;
}

export function useAiEvents(input: string | UseAiEventsOptions = {}) {
  const options = typeof input === 'string' ? { url: input, enabled: true } : input;
  const enabled = options.enabled ?? true;

  const backendBaseUrl = (import.meta.env.VITE_BACKEND_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
  const defaultWsUrl = backendBaseUrl.replace(/^http/, 'ws') + '/ws';
  const url = options.url ?? defaultWsUrl;

  const [events, setEvents] = useState<AiEvent[]>([]);

  useEffect(() => {
    if (!enabled) {
      setEvents([]);
      return undefined;
    }

    const isWebSocket = url.startsWith('ws://') || url.startsWith('wss://') || url.includes('/ws');

    if (isWebSocket) {
      console.log('[useAiEvents] Connecting to WebSocket:', url);
      const client = new SimpleStompClient({
        url: url,
        topic: '/topic/alerts',
        onMessage: (message) => {
          try {
            const timestampVal = typeof message.timestamp === 'string'
              ? Math.floor(new Date(message.timestamp).getTime() / 1000)
              : (typeof message.timestamp === 'number' ? message.timestamp : Math.floor(Date.now() / 1000));

            const normalizedData = {
              camera_id: message.camera_id ?? message.cameraId,
              event_type: message.event_type ?? message.type,
              timestamp: timestampVal,
              severity: message.severity ?? 'HIGH',
              frame_idx: message.frame_idx ?? 0,
              score: message.score ?? 0,
              confidence: message.confidence ?? 0,
              boxes: message.boxes ?? [],
              bbox: message.bbox ?? null,
              threshold: message.threshold ?? 0,
              track_id: message.track_id ?? null,
            };

            const parsed = aiEventSchema.parse(normalizedData);
            const normalized: AiEvent = {
              ...parsed,
              bbox: parsed.bbox ?? null,
              track_id: parsed.track_id === null || parsed.track_id === undefined ? null : String(parsed.track_id),
            };
            setEvents((prev) => [normalized, ...prev].slice(0, 50));
          } catch (error) {
            if (error instanceof SyntaxError || error instanceof z.ZodError) {
              console.warn('[useAiEvents] Ignoring malformed AI event payload:', error.message);
              return;
            }
            throw error;
          }
        },
        onStatusChange: (status) => {
          console.log('[useAiEvents] WebSocket status changed:', status);
        }
      });

      client.connect();

      return () => {
        client.disconnect();
      };
    } else {
      console.log('[useAiEvents] Connecting to SSE EventSource:', url);
      const eventSource = new EventSource(url);
      eventSource.onmessage = (event) => {
        try {
          const rawData = JSON.parse(event.data);
          const timestampVal = typeof rawData.timestamp === 'string'
            ? Math.floor(new Date(rawData.timestamp).getTime() / 1000)
            : (typeof rawData.timestamp === 'number' ? rawData.timestamp : Math.floor(Date.now() / 1000));

          const normalizedData = {
            camera_id: rawData.camera_id ?? rawData.cameraId,
            event_type: rawData.event_type ?? rawData.type,
            timestamp: timestampVal,
            severity: rawData.severity ?? 'HIGH',
            frame_idx: rawData.frame_idx ?? 0,
            score: rawData.score ?? 0,
            confidence: rawData.confidence ?? 0,
            boxes: rawData.boxes ?? [],
            bbox: rawData.bbox ?? null,
            threshold: rawData.threshold ?? 0,
            track_id: rawData.track_id ?? null,
          };

          const parsed = aiEventSchema.parse(normalizedData);
          const normalized: AiEvent = {
            ...parsed,
            bbox: parsed.bbox ?? null,
            track_id: parsed.track_id === null || parsed.track_id === undefined ? null : String(parsed.track_id),
          };
          setEvents((prev) => [normalized, ...prev].slice(0, 50));
        } catch (error) {
          if (error instanceof SyntaxError || error instanceof z.ZodError) {
            console.warn('[useAiEvents] Ignoring malformed AI event payload:', error.message);
            return;
          }
          throw error;
        }
      };

      eventSource.onerror = () => {
        console.warn('[useAiEvents] AI event stream disconnected.');
      };

      return () => {
        eventSource.close();
      };
    }
  }, [enabled, url]);

  return events;
}

