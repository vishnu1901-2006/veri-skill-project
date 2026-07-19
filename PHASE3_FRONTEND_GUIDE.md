# Phase 3: Frontend Interface - Implementation Guide

## 🎯 Objective

Build a React-based web interface for two user personas:
1. **Candidates** - Upload resumes for specific job postings
2. **Recruiters** - Create jobs and view verification results (WITHOUT seeing actual scores)

---

## 📋 Requirements Checklist

### Core Features
- [ ] Candidate upload form with drag-and-drop
- [ ] Real-time verification status (Pending → Verified/Failed)
- [ ] Recruiter job creation interface
- [ ] Recruiter dashboard showing candidates with badges
- [ ] Privacy enforcement (hide scores from recruiters)
- [ ] Responsive design (mobile + desktop)
- [ ] Error handling and user feedback

### Non-Functional Requirements
- [ ] Page load time < 2 seconds
- [ ] Accessibility (WCAG 2.1 AA)
- [ ] TypeScript strict mode
- [ ] Error boundaries
- [ ] Loading skeletons

---

## 🚀 Quick Start

### Step 1: Create React Project
```bash
cd /mnt/d/veri-skill-project
npx create-react-app veri-skill-frontend --template typescript
cd veri-skill-frontend
```

### Step 2: Install Dependencies
```bash
npm install \
  react-router-dom \
  axios \
  @tanstack/react-query \
  tailwindcss \
  @headlessui/react \
  @heroicons/react \
  react-dropzone \
  react-hot-toast
```

### Step 3: Initialize Tailwind CSS
```bash
npx tailwindcss init -p
```

### Step 4: Project Structure
```
veri-skill-frontend/
├── src/
│   ├── components/
│   │   ├── CandidateForm.tsx         # Resume upload form
│   │   ├── RecruiterDashboard.tsx    # Job + candidate view
│   │   ├── VerificationBadge.tsx     # ✓/✗ badge component
│   │   ├── LoadingSpinner.tsx        # Loading indicator
│   │   └── ErrorBoundary.tsx         # Error handling
│   ├── pages/
│   │   ├── CandidatePage.tsx         # Candidate flow
│   │   ├── RecruiterPage.tsx         # Recruiter flow
│   │   └── HomePage.tsx              # Landing page
│   ├── services/
│   │   ├── api.ts                    # Backend API client
│   │   └── types.ts                  # TypeScript types
│   ├── hooks/
│   │   ├── useUploadResume.ts        # Custom hook for upload
│   │   ├── useCreateJob.ts           # Custom hook for jobs
│   │   └── useVerification.ts        # Custom hook for verification
│   ├── App.tsx                       # Main app with routing
│   ├── App.css                       # Global styles
│   └── index.tsx                     # Entry point
├── tailwind.config.js                # Tailwind config
├── .env.local                        # Environment variables
└── README.md
```

---

## 📁 File Templates

### 1. `.env.local`
```env
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
REACT_APP_ZK_BRIDGE_URL=http://localhost:3000
```

### 2. `src/services/types.ts`
```typescript
// API Request/Response types
export interface Job {
  id: string;
  title: string;
  description: string;
  required_threshold: number;
  recruiterId: string;
  status: 'OPEN' | 'CLOSED';
  createdAt: string;
}

export interface CandidateUploadRequest {
  jobId: string;
  fullName: string;
  email: string;
  resume: File;
}

export interface VerificationResponse {
  id: string;
  email: string;
  fullName: string;
  jobId: string;
  verification_status: 'PENDING' | 'PROCESSING' | 'VERIFIED' | 'REJECTED';
  proof_verified: boolean;
  createdAt: string;
  message?: string;
}

export interface CreateJobRequest {
  title: string;
  description: string;
  required_threshold: number;
  recruiterId: string;
}
```

### 3. `src/services/api.ts`
```typescript
import axios, { AxiosInstance } from 'axios';
import { Job, VerificationResponse, CreateJobRequest, CandidateUploadRequest } from './types';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1';

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
});

// Job Management
export const jobAPI = {
  create: (job: CreateJobRequest) =>
    api.post<Job>('/jobs', job).then(res => res.data),
  
  getAll: (recruiterId: string) =>
    api.get<Job[]>('/jobs', { params: { recruiterId } }).then(res => res.data),
  
  getById: (jobId: string) =>
    api.get<Job>(`/jobs/${jobId}`).then(res => res.data),
};

// Candidate Management
export const candidateAPI = {
  upload: async (data: CandidateUploadRequest) => {
    const formData = new FormData();
    formData.append('jobId', data.jobId);
    formData.append('fullName', data.fullName);
    formData.append('email', data.email);
    formData.append('resume', data.resume);
    
    return api.post<VerificationResponse>('/candidates/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(res => res.data);
  },
  
  getVerification: (candidateId: string, jobId: string) =>
    api.get<VerificationResponse>(`/candidates/${candidateId}/verification`, {
      params: { jobId },
    }).then(res => res.data),
  
  getCandidatesForJob: (jobId: string) =>
    api.get<VerificationResponse[]>('/candidates', {
      params: { jobId },
    }).then(res => res.data),
};

export default api;
```

### 4. `src/hooks/useUploadResume.ts`
```typescript
import { useMutation, useQuery } from '@tanstack/react-query';
import { candidateAPI } from '../services/api';
import { VerificationResponse, CandidateUploadRequest } from '../services/types';

export const useUploadResume = () => {
  return useMutation({
    mutationFn: (data: CandidateUploadRequest) => candidateAPI.upload(data),
    onError: (error) => {
      console.error('Upload failed:', error);
    },
  });
};

export const useVerificationStatus = (candidateId: string | null, jobId: string) => {
  return useQuery<VerificationResponse>({
    queryKey: ['verification', candidateId, jobId],
    queryFn: () => candidateAPI.getVerification(candidateId!, jobId),
    enabled: !!candidateId,
    refetchInterval: 2000, // Poll every 2 seconds while processing
  });
};
```

### 5. `src/components/CandidateForm.tsx`
```typescript
import React, { useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { useUploadResume, useVerificationStatus } from '../hooks/useUploadResume';
import { VerificationBadge } from './VerificationBadge';
import toast from 'react-hot-toast';

interface CandidateFormProps {
  jobId: string;
  jobTitle: string;
}

export const CandidateForm: React.FC<CandidateFormProps> = ({ jobId, jobTitle }) => {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    resume: null as File | null,
  });
  const [candidateId, setCandidateId] = useState<string | null>(null);

  const uploadMutation = useUploadResume();
  const verificationQuery = useVerificationStatus(candidateId, jobId);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop: (files) => {
      if (files[0]) {
        setFormData(prev => ({ ...prev, resume: files[0] }));
      }
    },
    accept: { 'application/pdf': ['.pdf'] },
    maxSize: 10 * 1024 * 1024, // 10MB
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.fullName || !formData.email || !formData.resume) {
      toast.error('Please fill in all fields');
      return;
    }

    try {
      const result = await uploadMutation.mutateAsync({
        jobId,
        fullName: formData.fullName,
        email: formData.email,
        resume: formData.resume,
      });
      
      setCandidateId(result.id);
      toast.success('Resume submitted! Processing verification...');
    } catch (error) {
      toast.error('Upload failed. Please try again.');
      console.error(error);
    }
  };

  if (candidateId && verificationQuery.data) {
    return (
      <div className="max-w-md mx-auto p-6 bg-white rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-4">Verification Status</h3>
        <VerificationBadge status={verificationQuery.data.verification_status} />
        <p className="mt-4 text-sm text-gray-600">
          {verificationQuery.data.message || 'Verification complete'}
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-md mx-auto p-6 bg-white rounded-lg shadow">
      <h2 className="text-2xl font-bold mb-2">Apply for Position</h2>
      <p className="text-gray-600 mb-6">{jobTitle}</p>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Name Input */}
        <input
          type="text"
          placeholder="Full Name"
          value={formData.fullName}
          onChange={(e) => setFormData(prev => ({ ...prev, fullName: e.target.value }))}
          className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        {/* Email Input */}
        <input
          type="email"
          placeholder="Email Address"
          value={formData.email}
          onChange={(e) => setFormData(prev => ({ ...prev, email: e.target.value }))}
          className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        {/* File Upload */}
        <div
          {...getRootProps()}
          className={`border-2 border-dashed rounded-lg p-6 text-center cursor-pointer transition ${
            isDragActive ? 'border-blue-500 bg-blue-50' : 'border-gray-300'
          }`}
        >
          <input {...getInputProps()} />
          {formData.resume ? (
            <div>
              <p className="text-green-600 font-semibold">{formData.resume.name}</p>
              <p className="text-sm text-gray-500">Click to change</p>
            </div>
          ) : (
            <div>
              <p className="font-semibold">Drop resume here or click to browse</p>
              <p className="text-sm text-gray-500">PDF only, max 10MB</p>
            </div>
          )}
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={uploadMutation.isPending}
          className="w-full bg-blue-600 text-white py-2 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {uploadMutation.isPending ? 'Uploading...' : 'Submit Resume'}
        </button>
      </form>
    </div>
  );
};
```

### 6. `src/components/VerificationBadge.tsx`
```typescript
import React from 'react';
import { CheckCircleIcon, XCircleIcon } from '@heroicons/react/24/solid';

interface VerificationBadgeProps {
  status: 'PENDING' | 'PROCESSING' | 'VERIFIED' | 'REJECTED';
}

export const VerificationBadge: React.FC<VerificationBadgeProps> = ({ status }) => {
  const statusConfig = {
    PENDING: { color: 'bg-gray-100', textColor: 'text-gray-700', label: 'Pending' },
    PROCESSING: { color: 'bg-yellow-100', textColor: 'text-yellow-700', label: 'Processing...' },
    VERIFIED: { color: 'bg-green-100', textColor: 'text-green-700', label: 'Verified ✓' },
    REJECTED: { color: 'bg-red-100', textColor: 'text-red-700', label: 'Not Verified ✗' },
  };

  const config = statusConfig[status];
  const Icon = status === 'VERIFIED' ? CheckCircleIcon : XCircleIcon;

  return (
    <div className={`${config.color} ${config.textColor} px-4 py-3 rounded-lg flex items-center gap-2`}>
      {status === 'VERIFIED' || status === 'REJECTED' ? (
        <Icon className="w-5 h-5" />
      ) : (
        <div className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin" />
      )}
      <span className="font-semibold">{config.label}</span>
    </div>
  );
};
```

### 7. `src/pages/CandidatePage.tsx`
```typescript
import React, { useState } from 'react';
import { CandidateForm } from '../components/CandidateForm';
import { useQuery } from '@tanstack/react-query';
import { jobAPI } from '../services/api';

export const CandidatePage: React.FC = () => {
  const [selectedJobId, setSelectedJobId] = useState<string | null>(null);
  const jobsQuery = useQuery({
    queryKey: ['jobs'],
    queryFn: () => jobAPI.getAll(''), // Fetch all open jobs
  });

  if (jobsQuery.isLoading) {
    return <div className="text-center py-10">Loading jobs...</div>;
  }

  if (selectedJobId) {
    const job = jobsQuery.data?.find(j => j.id === selectedJobId);
    return (
      <div className="min-h-screen bg-gray-50 py-10">
        <div className="max-w-2xl mx-auto">
          <button
            onClick={() => setSelectedJobId(null)}
            className="mb-4 text-blue-600 hover:underline"
          >
            ← Back to Jobs
          </button>
          {job && <CandidateForm jobId={job.id} jobTitle={job.title} />}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-8">Available Positions</h1>
        <div className="grid gap-4">
          {jobsQuery.data?.map((job) => (
            <div key={job.id} className="bg-white p-6 rounded-lg shadow hover:shadow-lg transition cursor-pointer"
              onClick={() => setSelectedJobId(job.id)}>
              <h2 className="text-xl font-bold">{job.title}</h2>
              <p className="text-gray-600 mt-2">{job.description}</p>
              <p className="text-sm text-gray-500 mt-4">Min Score Required: {job.required_threshold}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
```

### 8. `src/pages/RecruiterPage.tsx`
```typescript
import React, { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { jobAPI, candidateAPI } from '../services/api';
import { VerificationBadge } from '../components/VerificationBadge';
import toast from 'react-hot-toast';

export const RecruiterPage: React.FC = () => {
  const [recruiterId] = useState('recruiter-001'); // Replace with actual user
  const [selectedJobId, setSelectedJobId] = useState<string | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);

  // Fetch jobs
  const jobsQuery = useQuery({
    queryKey: ['jobs', recruiterId],
    queryFn: () => jobAPI.getAll(recruiterId),
  });

  // Fetch candidates for selected job
  const candidatesQuery = useQuery({
    queryKey: ['candidates', selectedJobId],
    queryFn: () => selectedJobId ? candidateAPI.getCandidatesForJob(selectedJobId) : Promise.resolve([]),
    enabled: !!selectedJobId,
  });

  // Create job mutation
  const createJobMutation = useMutation({
    mutationFn: (jobData: any) => jobAPI.create({ ...jobData, recruiterId }),
    onSuccess: () => {
      toast.success('Job created successfully!');
      setShowCreateForm(false);
      jobsQuery.refetch();
    },
    onError: () => {
      toast.error('Failed to create job');
    },
  });

  return (
    <div className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-6xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold">Recruiter Dashboard</h1>
          <button
            onClick={() => setShowCreateForm(!showCreateForm)}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
          >
            + Create Job
          </button>
        </div>

        {/* Create Job Form */}
        {showCreateForm && (
          <div className="bg-white p-6 rounded-lg shadow mb-8">
            <h2 className="text-xl font-bold mb-4">Create New Job</h2>
            <form onSubmit={(e) => {
              e.preventDefault();
              const formData = new FormData(e.currentTarget);
              createJobMutation.mutate({
                title: formData.get('title'),
                description: formData.get('description'),
                required_threshold: parseInt(formData.get('threshold') as string),
              });
            }} className="space-y-4">
              <input type="text" name="title" placeholder="Job Title" required className="w-full border rounded px-4 py-2" />
              <textarea name="description" placeholder="Job Description" required className="w-full border rounded px-4 py-2" />
              <input type="number" name="threshold" placeholder="Required Score (0-100)" required className="w-full border rounded px-4 py-2" />
              <button type="submit" disabled={createJobMutation.isPending} className="bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700 disabled:opacity-50">
                {createJobMutation.isPending ? 'Creating...' : 'Create Job'}
              </button>
            </form>
          </div>
        )}

        {/* Jobs and Candidates */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Jobs List */}
          <div className="lg:col-span-1">
            <h2 className="text-xl font-bold mb-4">My Jobs</h2>
            <div className="space-y-2">
              {jobsQuery.data?.map((job) => (
                <button
                  key={job.id}
                  onClick={() => setSelectedJobId(job.id)}
                  className={`w-full text-left p-4 rounded-lg border-2 transition ${
                    selectedJobId === job.id
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-200 hover:border-blue-300'
                  }`}
                >
                  <p className="font-semibold">{job.title}</p>
                  <p className="text-sm text-gray-600">Threshold: {job.required_threshold}</p>
                </button>
              ))}
            </div>
          </div>

          {/* Candidates List */}
          <div className="lg:col-span-2">
            {selectedJobId ? (
              <div>
                <h2 className="text-xl font-bold mb-4">Candidates</h2>
                <div className="space-y-3">
                  {candidatesQuery.data?.map((candidate) => (
                    <div key={candidate.id} className="bg-white p-4 rounded-lg shadow flex justify-between items-center">
                      <div>
                        <p className="font-semibold">{candidate.fullName}</p>
                        <p className="text-sm text-gray-600">{candidate.email}</p>
                      </div>
                      <VerificationBadge status={candidate.verification_status} />
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="text-center text-gray-500 py-10">
                Select a job to view candidates
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
```

### 9. `src/App.tsx`
```typescript
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { CandidatePage } from './pages/CandidatePage';
import { RecruiterPage } from './pages/RecruiterPage';
import './App.css';

const queryClient = new QueryClient();

const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/candidate" element={<CandidatePage />} />
          <Route path="/recruiter" element={<RecruiterPage />} />
        </Routes>
      </BrowserRouter>
      <Toaster position="top-right" />
    </QueryClientProvider>
  );
};

const HomePage: React.FC = () => (
  <div className="min-h-screen bg-gradient-to-r from-blue-600 to-purple-600 flex items-center justify-center">
    <div className="text-center text-white">
      <h1 className="text-5xl font-bold mb-4">Veri-Skill</h1>
      <p className="text-xl mb-8">Zero-Knowledge Resume Verification</p>
      <div className="space-x-4">
        <a href="/candidate" className="bg-white text-blue-600 px-6 py-3 rounded-lg font-bold hover:bg-gray-100">
          I'm a Candidate
        </a>
        <a href="/recruiter" className="bg-purple-700 text-white px-6 py-3 rounded-lg font-bold hover:bg-purple-800">
          I'm a Recruiter
        </a>
      </div>
    </div>
  </div>
);

export default App;
```

---

## 🎨 Tailwind Configuration

Update `tailwind.config.js`:
```javascript
module.exports = {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        'brand-blue': '#1e40af',
        'brand-purple': '#7c3aed',
      },
    },
  },
  plugins: [],
};
```

---

## 🚀 Running the Frontend

```bash
cd veri-skill-frontend

# Development
npm start
# 🟢 Open http://localhost:3000

# Build for production
npm run build

# Test
npm test
```

---

## 🔐 Privacy & Security

### Critical Rule: HIDE SCORES FROM RECRUITERS
```typescript
// ❌ WRONG - Never do this
recruiters can see: { score: 82, verified: true }

// ✅ CORRECT - Only show verification status
recruiters can see: { verified: true, message: "Verified" }
```

### API Response Filtering
In Spring Boot backend, implement response filtering:
```java
// CandidateController.java
public ResponseEntity<?> getVerification(@PathVariable String candidateId) {
    Candidate candidate = service.getCandidate(candidateId);
    
    // NEVER include ai_score in response to recruiters
    return ResponseEntity.ok(new VerificationResponse(
        candidate.getVerificationStatus(),
        candidate.isProofVerified(),
        "Candidate verification complete"
        // NO ai_score field!
    ));
}
```

---

## 📊 State Management

Use React Query for:
- Server state (API responses)
- Caching
- Background refetching

Use React hooks for:
- UI state (form inputs, modals)
- Local state (selected job)

---

## ♿ Accessibility

- [ ] All images have alt text
- [ ] Form labels associate with inputs (`htmlFor`)
- [ ] Color not sole differentiator (use text + icons)
- [ ] Keyboard navigation works
- [ ] Focus states visible
- [ ] ARIA labels where needed

---

## 🧪 Testing Strategy

### Unit Tests
```typescript
// src/components/__tests__/VerificationBadge.test.tsx
import { render, screen } from '@testing-library/react';
import { VerificationBadge } from '../VerificationBadge';

describe('VerificationBadge', () => {
  it('shows verified status', () => {
    render(<VerificationBadge status="VERIFIED" />);
    expect(screen.getByText('Verified ✓')).toBeInTheDocument();
  });

  it('shows processing status', () => {
    render(<VerificationBadge status="PROCESSING" />);
    expect(screen.getByText('Processing...')).toBeInTheDocument();
  });
});
```

### Integration Tests
```typescript
// Test candidate upload flow
// 1. User selects job
// 2. User fills form
// 3. User uploads resume
// 4. API is called
// 5. Verification status updates
```

---

## 🚨 Error Handling

```typescript
const handleUpload = async () => {
  try {
    const result = await uploadMutation.mutateAsync(formData);
    toast.success('Upload successful!');
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 413) {
        toast.error('File too large (max 10MB)');
      } else if (error.response?.status === 400) {
        toast.error('Invalid PDF file');
      } else {
        toast.error('Upload failed. Please try again.');
      }
    }
  }
};
```

---

## 📱 Responsive Design

```typescript
// Mobile-first approach
<div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
  {/* 1 column on mobile, 3 columns on desktop */}
</div>
```

---

## 🔄 Environment Setup

### `.env.local` for local development
```env
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
REACT_APP_ZK_BRIDGE_URL=http://localhost:3000
REACT_APP_DEBUG=true
```

### `.env.production` for deployment
```env
REACT_APP_API_BASE_URL=https://api.veriskill.dev/api/v1
REACT_APP_ZK_BRIDGE_URL=https://zk.veriskill.dev
REACT_APP_DEBUG=false
```

---

## ✅ Checklist Before Deployment

- [ ] All components render without errors
- [ ] API calls working (test with Network tab open)
- [ ] Scores never appear in network requests to recruiter endpoints
- [ ] Mobile layout responsive
- [ ] Loading states display correctly
- [ ] Error messages clear and helpful
- [ ] No console errors or warnings
- [ ] TypeScript strict mode passes
- [ ] Tests pass
- [ ] Accessibility check passes (axe DevTools)
- [ ] Performance acceptable (Lighthouse > 90)

---

## 📚 Next Steps

1. **Create React project** using the template above
2. **Set up API client** (`src/services/api.ts`)
3. **Build core components** (Form, Badge, Dashboard)
4. **Test with running backend** (Spring Boot + Flask)
5. **Deploy frontend** to Vercel or similar
6. **Monitor performance** and user feedback

---

## 🆘 Troubleshooting

### "CORS errors"
```typescript
// Backend Spring Boot needs CORS enabled
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class CandidateController { ... }
```

### "Files not uploading"
- Check file size < 10MB
- Verify API endpoint URL in `.env.local`
- Check network tab for error response

### "Verification status not updating"
- React Query refetch interval is 2 seconds
- Check backend is returning updated status
- Verify `useVerificationStatus` hook enabled correctly

---

## 🎓 Learning Resources

- [React Docs](https://react.dev)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Tailwind CSS](https://tailwindcss.com)
- [React Query Docs](https://tanstack.com/query/latest)
- [React Router](https://reactrouter.com)

---

**Status:** Ready to implement  
**Difficulty:** Medium  
**Estimated Time:** 8-12 hours  
**Team:** Frontend Engineer(s)

This guide covers all essential components. Let me know when you're ready to start implementation!
