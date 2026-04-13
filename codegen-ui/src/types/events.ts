export type ContextEntry = {
  role: string
  content: string
}

export type SessionState = {
  id: string
  currentIntermediateModelData: string
  currentBpmnData: string
  // Extended fields — present in fuller API responses
  events?: ContextEntry[]
  totalTokensUsed?: number
  estimatedCompressedTokenSize?: number
  estimatedUncompressedTokenSize?: number
  lastPrompt?: string
  lastResponse?: string
  validOutput?: boolean
  validationErrors?: string[]
  iterationsRequired?: number
  currentTemperature?: number
  transformedContent?: string
}

export type PromptRequest = {
  prompt: string
  temperature: number
}
