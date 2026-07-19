import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom';
import { BriefcaseBusiness, ShieldCheck, Upload } from 'lucide-react';
import { ErrorBoundary } from './components/ErrorBoundary';
import { SystemStatus } from './components/SystemStatus';
import { CandidatePage } from './pages/CandidatePage';
import { RecruiterPage } from './pages/RecruiterPage';

function HomePage() {
  return (
    <main className="min-h-screen bg-surface">
      <section className="border-b border-line bg-white">
        <div className="mx-auto flex min-h-[72vh] max-w-6xl flex-col justify-center px-4 py-12 sm:px-6 lg:px-8">
          <div className="max-w-3xl">
            <div className="mb-5 inline-flex items-center gap-2 rounded-full border border-line bg-surface px-3 py-1 text-sm font-medium text-slate-700">
              <ShieldCheck className="h-4 w-4 text-blue-700" aria-hidden="true" />
              Privacy-preserving resume verification
            </div>
            <h1 className="text-4xl font-semibold tracking-normal text-ink sm:text-5xl">
              Veri-Skill
            </h1>
            <p className="mt-5 max-w-2xl text-lg leading-8 text-slate-600">
              Upload resumes, score them through the AI service, and show recruiters only
              the ZK-backed verification result.
            </p>
            <div className="mt-8 grid max-w-2xl gap-3 sm:grid-cols-2">
              <Link
                to="/candidate"
                className="group flex min-h-28 items-center gap-4 rounded-lg border border-line bg-white p-5 shadow-panel transition hover:border-blue-300 hover:bg-blue-50"
              >
                <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-md bg-blue-700 text-white">
                  <Upload className="h-5 w-5" aria-hidden="true" />
                </span>
                <span>
                  <span className="block font-semibold text-ink">Candidate</span>
                  <span className="mt-1 block text-sm leading-6 text-slate-600">
                    Submit a resume for a job ID.
                  </span>
                </span>
              </Link>
              <Link
                to="/recruiter"
                className="group flex min-h-28 items-center gap-4 rounded-lg border border-line bg-white p-5 shadow-panel transition hover:border-emerald-300 hover:bg-emerald-50"
              >
                <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-md bg-emerald-700 text-white">
                  <BriefcaseBusiness className="h-5 w-5" aria-hidden="true" />
                </span>
                <span>
                  <span className="block font-semibold text-ink">Recruiter</span>
                  <span className="mt-1 block text-sm leading-6 text-slate-600">
                    Create jobs and review verified status.
                  </span>
                </span>
              </Link>
            </div>
          </div>
        </div>
      </section>
      <section className="mx-auto max-w-6xl px-4 py-6 sm:px-6 lg:px-8">
        <SystemStatus />
      </section>
    </main>
  );
}

function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/candidate" element={<CandidatePage />} />
          <Route path="/recruiter" element={<RecruiterPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </ErrorBoundary>
  );
}

export default App;
