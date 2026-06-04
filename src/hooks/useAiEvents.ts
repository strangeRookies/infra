import { useState, useEffect } from 'react';

export interface AiEvent {
  camera_id: string;
  frame_idx: number;
  timestamp: number;
  event_type: string;
  score: number;
  confidence: number;
  boxes: any[];
  bbox: any;
  threshold: number;
  track_id: string | null;
  severity: string;
}

export function useAiEvents(url: string = 'http://localhost:8010/events') {
  const [events, setEvents] = useState<AiEvent[]>([]);

  useEffect(() => {
    const eventSource = new EventSource(url);

    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        setEvents((prev) => [data, ...prev].slice(0, 50)); // 최신 50개 유지
      } catch (err) {
        console.error('Error parsing AI event:', err);
      }
    };

    eventSource.onerror = (error) => {
      console.error('EventSource failed:', error);
    };

    return () => {
      eventSource.close();
    };
  }, [url]);

  return events;
}
