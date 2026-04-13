import { apiClient } from './client'
import type { SessionState, PromptRequest } from '../types/events'

export const generateBpmn = (sessionId: string, request: PromptRequest): Promise<SessionState> =>
  apiClient
    .post<SessionState>(`/api/bpmn/generation/session/${sessionId}/prompt`, request)
    .then((r) => r.data)

export const generateCode = (sessionId: string, request: PromptRequest): Promise<SessionState> =>
  apiClient
    .post<SessionState>(`/api/gpt/session/${sessionId}/prompt/groovy`, request)
    .then((r) => r.data)

export const getSessionState = (sessionId: string): Promise<SessionState> =>
  apiClient
    .get<SessionState>(`/api/gpt/session/${sessionId}/state`)
    .then((r) => r.data)
