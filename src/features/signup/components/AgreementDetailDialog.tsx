import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../../shared/components/ui/dialog';
import type { AgreementItem } from '../data/agreements';

interface AgreementDetailDialogProps {
  agreement?: AgreementItem;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function AgreementDetailDialog({ agreement, open, onOpenChange }: AgreementDetailDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[86vh] max-w-[min(92vw,680px)] overflow-hidden border-slate-800 bg-[#0a1224] p-0 text-slate-100">
        {agreement && (
          <>
            <DialogHeader className="border-b border-slate-800 px-5 py-4 text-left">
              <div className="flex flex-wrap items-center gap-2">
                <span
                  className={`rounded-md border px-2 py-0.5 text-[10px] font-extrabold ${
                    agreement.required
                      ? 'border-rose-500/30 bg-rose-500/10 text-rose-300'
                      : 'border-blue-500/30 bg-blue-500/10 text-blue-300'
                  }`}
                >
                  {agreement.required ? '필수' : '선택'}
                </span>
                <DialogTitle className="text-base font-extrabold text-white">
                  {agreement.title}
                </DialogTitle>
              </div>
              <DialogDescription className="text-xs text-slate-400">
                내용을 확인한 뒤 동의 여부를 선택해주세요.
              </DialogDescription>
            </DialogHeader>

            <div className="max-h-[56vh] overflow-y-auto px-5 py-4">
              <div className="space-y-5">
                {agreement.content.map((section) => (
                  <section key={section.heading} className="space-y-2">
                    <h3 className="text-xs font-extrabold text-slate-200">{section.heading}</h3>
                    <div className="space-y-2">
                      {section.body.map((paragraph) => (
                        <p key={paragraph} className="text-xs leading-6 text-slate-400">
                          {paragraph}
                        </p>
                      ))}
                    </div>
                  </section>
                ))}
              </div>
            </div>

            <DialogFooter className="border-t border-slate-800 px-5 py-4">
              <DialogClose className="w-full rounded-xl bg-blue-600 px-4 py-3 text-xs font-bold text-white transition-colors hover:bg-blue-500 sm:w-auto">
                확인
              </DialogClose>
            </DialogFooter>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
