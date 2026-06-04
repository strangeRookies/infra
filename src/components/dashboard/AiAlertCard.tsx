import React from 'react';
import { AlertTriangle, Clock, Camera } from 'lucide-react';
import { AiEvent } from '../../hooks/useAiEvents';

interface AiAlertCardProps {
  event: AiEvent;
}

export const AiAlertCard: React.FC<AiAlertCardProps> = ({ event }) => {
  const isDanger = event.event_type !== 'Normal';
  
  if (!isDanger) return null;

  const time = new Date(event.timestamp * 1000).toLocaleTimeString();

  return (
    <div className="relative overflow-hidden rounded-xl bg-red-900/30 border border-red-500/50 p-4 mb-3 backdrop-blur-md shadow-[0_0_15px_rgba(239,68,68,0.3)] animate-in fade-in slide-in-from-right-4 duration-300 group">
      {/* Pulse background effect */}
      <div className="absolute inset-0 bg-red-500/10 animate-pulse"></div>
      
      <div className="relative z-10 flex items-start gap-3">
        <div className="p-2 bg-red-500/20 rounded-full text-red-400">
          <AlertTriangle className="w-5 h-5" />
        </div>
        
        <div className="flex-1">
          <div className="flex justify-between items-start">
            <h4 className="font-bold text-red-400 text-lg">{event.event_type} Detected</h4>
            <span className="text-xs font-mono text-red-300/80 bg-red-950/50 px-2 py-1 rounded-md border border-red-500/20">
              {(event.confidence * 100).toFixed(1)}%
            </span>
          </div>
          
          <div className="mt-2 flex items-center gap-4 text-xs text-red-200/70">
            <div className="flex items-center gap-1">
              <Camera className="w-3.5 h-3.5" />
              <span>{event.camera_id}</span>
            </div>
            <div className="flex items-center gap-1">
              <Clock className="w-3.5 h-3.5" />
              <span>{time}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
