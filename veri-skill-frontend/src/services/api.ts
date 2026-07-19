import axios from 'axios';
import type {
  CandidateResponse,
  CandidateUploadRequest,
  CreateJobRequest,
  HealthResponse,
  Job,
  VerificationResponse,
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
});

export const systemAPI = {
  health: async () => {
    const response = await axios.get<HealthResponse>('/actuator/health', {
      timeout: 5000,
    });
    return response.data;
  },
};

export const jobAPI = {
  create: async (payload: CreateJobRequest) => {
    const response = await api.post<Job>('/jobs', payload);
    return response.data;
  },

  getById: async (jobId: string) => {
    const response = await api.get<Job>(`/jobs/${jobId}`);
    return response.data;
  },

  getByRecruiter: async (recruiterId: string) => {
    const response = await api.get<Job[]>(`/jobs/recruiter/${recruiterId}`);
    return response.data;
  },

  close: async (jobId: string) => {
    const response = await api.put<Job>(`/jobs/${jobId}/close`);
    return response.data;
  },
};

export const candidateAPI = {
  upload: async (payload: CandidateUploadRequest) => {
    const formData = new FormData();
    formData.append('jobId', payload.jobId);
    formData.append('email', payload.email);
    formData.append('fullName', payload.fullName);
    formData.append('resume', payload.resume);

    const response = await api.post<CandidateResponse>('/candidates/upload', formData);
    return response.data;
  },

  getCandidate: async (candidateId: string, jobId: string) => {
    const response = await api.get<CandidateResponse>(`/candidates/${candidateId}`, {
      params: { jobId },
    });
    return response.data;
  },

  getVerification: async (candidateId: string, jobId: string) => {
    const response = await api.get<VerificationResponse>(
      `/candidates/${candidateId}/verification`,
      { params: { jobId } }
    );
    return response.data;
  },
};
