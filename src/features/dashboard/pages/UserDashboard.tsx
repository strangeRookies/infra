import { useCallback, useMemo, useState } from 'react';
import { Camera, LogOut, Shield, ShieldAlert } from 'lucide-react';
import type { Inquiry } from '../../../shared/types/inquiry';
import { useAiAlertActions } from '../../../hooks/useAiAlertActions';
import { useDashboardAlerts } from '../hooks/useDashboardAlerts';
import { useLiveCameras } from '../hooks/useLiveCameras';
import type { MenuId, RegisteredCamera, InquiryCategory, IncidentAlert } from '../types/dashboard';
import {
  ALL_MENU_ITEMS,
  CATEGORIES,
  INITIAL_CAMERAS,
} from '../utils/dashboardStatus';
import { DashboardAlertsView } from '../components/DashboardAlertsView';
import { DashboardCameraManagementView } from '../components/DashboardCameraManagementView';
import { DashboardHistoryView } from '../components/DashboardHistoryView';
import { DashboardHomeView } from '../components/DashboardHomeView';
import { DashboardMyPageView } from '../components/DashboardMyPageView';
import { DashboardQnaView } from '../components/DashboardQnaView';
import { AddCameraModal } from '../modals/AddCameraModal';
import { IncidentPlaybackModal } from '../modals/IncidentPlaybackModal';
import { NewInquiryModal } from '../modals/NewInquiryModal';

interface NurseDashboardProps {
  username: string;
  userType: 'individual' | 'corporate';
  onLogout: () => void;
  inquiries: Inquiry[];
  onAddInquiry: (data: Omit<Inquiry, 'id' | 'createdAt'>) => void;
}

export function NurseDashboard({
  username,
  userType,
  onLogout,
  inquiries,
  onAddInquiry,
}: NurseDashboardProps) {
  const liveCameras = useLiveCameras();
  const [activeMenu, setActiveMenu] = useState<MenuId>('home');
  const [searchDate, setSearchDate] = useState<'today' | 'week' | 'month'>('month');
  const [searchCamera, setSearchCamera] = useState('전체');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedIncident, setSelectedIncident] = useState<IncidentAlert | null>(null);
  const [isPlaying, setIsPlaying] = useState(true);
  const [playbackProgress, setPlaybackProgress] = useState(30);
  const [registeredCameras, setRegisteredCameras] = useState<RegisteredCamera[]>(INITIAL_CAMERAS);
  const [showAddCamera, setShowAddCamera] = useState(false);
  const [newCamName, setNewCamName] = useState('');
  const [newCamId, setNewCamId] = useState('');
  const [newCamLocation, setNewCamLocation] = useState('');
  const [newCamPassword, setNewCamPassword] = useState('');
  const [showNewCamPw, setShowNewCamPw] = useState(false);
  const [showCamPwId, setShowCamPwId] = useState<string | null>(null);

  const [selectedQnaId, setSelectedQnaId] = useState<string | null>(null);
  const [showNewQnaModal, setShowNewQnaModal] = useState(false);
  const [qnaTitle, setQnaTitle] = useState('');
  const [qnaContent, setQnaContent] = useState('');
  const [qnaCategory, setQnaCategory] = useState<InquiryCategory>(CATEGORIES[3]);

  const focusHome = useCallback(() => setActiveMenu('home'), []);
  const {
    acknowledgedAiEventIds,
    dangerAiEvents,
    focusedLiveCameras,
    focusAiEventCamera,
    handleConfirmAiEvent,
    setFocusedCameraId,
    connectionState,
  } = useAiAlertActions({ userType, username, liveCameras, focusHome });

  const {
    alerts,
    activeTenMinAlerts,
    getFilteredHistory,
    resolveAlert,
  } = useDashboardAlerts({
    acknowledgedAiEventIds,
    dangerAiEvents,
    liveCameras,
    onConfirmAiEvent: handleConfirmAiEvent,
  });

  const filteredHistory = useMemo(
    () => getFilteredHistory({ searchDate, searchCamera, searchKeyword }),
    [getFilteredHistory, searchCamera, searchDate, searchKeyword],
  );
  const myInquiries = useMemo(
    () => inquiries.filter((inquiry) => inquiry.username === username),
    [inquiries, username],
  );

  const handleOpenIncident = (alert: IncidentAlert) => {
    setSelectedIncident(alert);
  };

  const handleCameraClick = (camera: { id: string; location: string; name: string }) => {
    setFocusedCameraId(camera.id);
    const matchingAlert = alerts.find((alert) => alert.camera === camera.location || alert.camera === camera.name);
    if (matchingAlert) setSelectedIncident(matchingAlert);
  };

  const handleTriggerEmergency = () => {
    const ok = window.confirm('119 긴급 출동 요청을 전송할까요?');
    if (ok) alert('긴급 출동 요청을 전송했습니다.');
  };

  const handleAddCamera = () => {
    if (!newCamName.trim() || !newCamId.trim()) return;
    setRegisteredCameras((prev) => [
      ...prev,
      {
        id: newCamId.trim(),
        name: newCamName.trim(),
        location: newCamLocation.trim() || '미지정',
        password: newCamPassword.trim() || undefined,
      },
    ]);
    setNewCamName('');
    setNewCamId('');
    setNewCamLocation('');
    setNewCamPassword('');
    setShowNewCamPw(false);
    setShowAddCamera(false);
  };

  const handleSubmitQna = () => {
    if (!qnaTitle.trim() || !qnaContent.trim()) return;
    onAddInquiry({
      userId: username,
      username,
      userType,
      category: qnaCategory,
      title: qnaTitle.trim(),
      content: qnaContent.trim(),
    });
    setQnaTitle('');
    setQnaContent('');
    setQnaCategory(CATEGORIES[3]);
    setShowNewQnaModal(false);
  };



  const selectedCameraObj = selectedIncident
    ? liveCameras.find((camera) => camera.name === selectedIncident.camera || camera.location === selectedIncident.camera)
    : null;
  const playbackStreamUrl = selectedCameraObj?.streamUrl || liveCameras[0]?.streamUrl;

  return (
    <div className="min-h-screen bg-[#020817] text-slate-100 flex flex-col font-sans">
      <header className="h-14 bg-[#061224] border-b border-slate-800/60 px-6 flex items-center justify-between z-10 flex-shrink-0">
        <div className="flex items-center gap-3">
          <Shield className="w-5 h-5 text-blue-400 fill-blue-400/20" />
          <h1 className="text-sm font-extrabold tracking-wider text-white">쉴더스 관제 대시보드</h1>
          <span className="h-4 w-px bg-slate-700" />
          <span className="text-xs font-bold text-slate-400">
            {userType === 'individual' ? '개인용 대시보드' : '기업용 대시보드'}
          </span>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 bg-rose-500/10 border border-rose-500/20 px-3 py-1 rounded-full">
            <span className="w-1.5 h-1.5 rounded-full bg-rose-500 animate-ping" />
            <span className="text-[10px] font-bold text-rose-400">확인 대기 이벤트 {activeTenMinAlerts.length}건</span>
          </div>
          <div
            className="flex items-center gap-2 ml-2 px-2 py-0.5 rounded text-[10px] font-bold"
            style={{
              color: connectionState === 'connected'
                ? '#34d399'
                : connectionState === 'connecting'
                  ? '#fbbf24'
                  : '#94a3b8',
            }}
          >
            <span
              className="w-1.5 h-1.5 rounded-full"
              style={{
                background: connectionState === 'connected'
                  ? '#34d399'
                  : connectionState === 'connecting'
                    ? '#fbbf24'
                    : '#64748b',
              }}
            />
            {connectionState === 'connected' ? '연결됨' : connectionState === 'connecting' ? '연결 중' : '연결 끊김'}
          </div>
          <div className="flex items-center gap-2.5">
            <div className="text-right">
              <span className="text-xs font-bold text-slate-200 block">{username || '사용자'}</span>
              <span className="text-[10px] text-slate-500 font-semibold">
                {userType === 'individual' ? '개인' : '기업'}
              </span>
            </div>
            <button onClick={onLogout} className="p-2 hover:bg-slate-800 rounded-xl text-slate-400 hover:text-white transition-all cursor-pointer" title="로그아웃">
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        <aside className="w-56 bg-[#071329] border-r border-slate-800/50 flex flex-col flex-shrink-0">
          <div className="p-4 space-y-1 flex-1">
            <h3 className="text-[10px] font-bold text-slate-500 uppercase tracking-widest px-3 mb-3">메뉴</h3>
            <nav className="space-y-0.5">
              {ALL_MENU_ITEMS.filter((item) => !item.individualOnly || userType === 'individual').map(({ id, label, icon: Icon }) => {
                const isActive = activeMenu === id;
                const badge = id === 'alerts' ? activeTenMinAlerts.length : undefined;
                return (
                  <button
                    key={id}
                    onClick={() => setActiveMenu(id)}
                    className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                      isActive ? 'bg-[#0758D6] text-white shadow-lg' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/30'
                    }`}
                  >
                    <div className="flex items-center gap-2.5">
                      <Icon className="w-4 h-4" />
                      <span>{label}</span>
                    </div>
                    {badge !== undefined && badge > 0 && (
                      <span className={`text-[9px] font-bold px-1.5 py-0.5 rounded-full ${isActive ? 'bg-white text-[#0758D6]' : 'bg-rose-500 text-white'}`}>
                        {badge}
                      </span>
                    )}
                  </button>
                );
              })}
            </nav>

            <div className="mt-8 pt-6 border-t border-slate-800/50 space-y-4 px-2">
              <h3 className="text-[10px] font-bold text-slate-500 uppercase tracking-widest px-1 mb-2">상태</h3>
              {userType === 'corporate' && (
                <div className="bg-[#0f172a] rounded-xl p-3 border border-slate-800/50">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-[9px] font-bold text-slate-400 uppercase">카메라 연결</span>
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  </div>
                  <div className="flex items-end gap-1.5">
                    <Camera className="w-4 h-4 text-blue-400 mb-0.5" />
                    <span className="text-sm font-extrabold text-white">
                      {liveCameras.filter((camera) => camera.connectionStatus === 'online').length}/{liveCameras.length}
                    </span>
                    <span className="text-[9px] text-slate-500 font-bold mb-0.5">대</span>
                  </div>
                </div>
              )}
              <div className="bg-[#0f172a] rounded-xl p-3 border border-slate-800/50">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-[9px] font-bold text-slate-400 uppercase">확인 대기 이벤트</span>
                  {activeTenMinAlerts.length > 0 && (
                    <span className="text-[8px] font-bold bg-rose-500 text-white px-1 rounded-sm animate-bounce">NEW</span>
                  )}
                </div>
                <div className="flex items-end gap-1.5">
                  <ShieldAlert className="w-4 h-4 text-rose-500 mb-0.5" />
                  <span className="text-sm font-extrabold text-white">{activeTenMinAlerts.length}</span>
                  <span className="text-[9px] text-slate-500 font-bold mb-0.5">건</span>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <main className="flex-1 flex overflow-hidden">
          {activeMenu === 'home' && (
            <DashboardHomeView
              acknowledgedAiEventIds={acknowledgedAiEventIds}
              dangerAiEvents={dangerAiEvents}
              focusedLiveCameras={focusedLiveCameras}
              onCameraSelect={handleCameraClick}
              onConfirmAiEvent={handleConfirmAiEvent}
              onEmergency={handleTriggerEmergency}
              onFocusAiEvent={focusAiEventCamera}
            />
          )}
          {activeMenu === 'alerts' && (
            <DashboardAlertsView
              alerts={activeTenMinAlerts}
              onOpenIncident={handleOpenIncident}
              onResolveAlert={resolveAlert}
            />
          )}
          {activeMenu === 'history' && (
            <DashboardHistoryView
              filteredHistory={filteredHistory}
              searchCamera={searchCamera}
              searchDate={searchDate}
              searchKeyword={searchKeyword}
              cameraOptions={registeredCameras}
              onOpenIncident={handleOpenIncident}
              onSearchCameraChange={setSearchCamera}
              onSearchDateChange={setSearchDate}
              onSearchKeywordChange={setSearchKeyword}
            />
          )}
          {activeMenu === 'cameras' && (
            <DashboardCameraManagementView
              liveCameras={liveCameras}
              registeredCameras={registeredCameras}
              showCamPwId={showCamPwId}
              onAddCamera={() => setShowAddCamera(true)}
              onDeleteCamera={(cameraId) => setRegisteredCameras((prev) => prev.filter((camera) => camera.id !== cameraId))}
              onTogglePassword={(cameraId) => setShowCamPwId((prev) => (prev === cameraId ? null : cameraId))}
            />
          )}
          {activeMenu === 'mypage' && (
            <DashboardMyPageView
              userType={userType}
              username={username}
            />
          )}
          {activeMenu === 'qna' && (
            <DashboardQnaView
              inquiries={myInquiries}
              selectedQnaId={selectedQnaId}
              onBack={() => setSelectedQnaId(null)}
              onCreateInquiry={() => setShowNewQnaModal(true)}
              onSelectQna={setSelectedQnaId}
            />
          )}
        </main>
      </div>

      {showAddCamera && (
        <AddCameraModal
          newCamId={newCamId}
          newCamLocation={newCamLocation}
          newCamName={newCamName}
          newCamPassword={newCamPassword}
          showNewCamPw={showNewCamPw}
          onClose={() => {
            setShowAddCamera(false);
            setShowNewCamPw(false);
          }}
          onIdChange={setNewCamId}
          onLocationChange={setNewCamLocation}
          onNameChange={setNewCamName}
          onPasswordChange={setNewCamPassword}
          onSubmit={handleAddCamera}
          onTogglePassword={() => setShowNewCamPw((prev) => !prev)}
        />
      )}

      {showNewQnaModal && (
        <NewInquiryModal
          qnaCategory={qnaCategory}
          qnaContent={qnaContent}
          qnaTitle={qnaTitle}
          onCategoryChange={setQnaCategory}
          onClose={() => setShowNewQnaModal(false)}
          onContentChange={setQnaContent}
          onSubmit={handleSubmitQna}
          onTitleChange={setQnaTitle}
        />
      )}

      {selectedIncident && (
        <IncidentPlaybackModal
          incident={selectedIncident}
          isPlaying={isPlaying}
          playbackProgress={playbackProgress}
          playbackStreamUrl={playbackStreamUrl}
          onClose={() => setSelectedIncident(null)}
          onPlaybackProgressChange={setPlaybackProgress}
          onTogglePlaying={() => setIsPlaying((prev) => !prev)}
        />
      )}
    </div>
  );
}
