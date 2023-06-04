export type ContextEntry = {
    role: string,
    content: string
}

export type SessionState = {
    id: string,
    events: ContextEntry[],
    totalTokensUsed: int,
    estimatedCompressedTokenSize: int,
    estimatedUncompressedTokenSize: int,
    lastPrompt: string,
    lastResponse: string,
    validOutput: boolean,
    validationErrors: string[],
    iterationsRequired: int
}

export type GenerateResponseData = {
    
}

export type Data = {
    key: string,
    offset: int,
    value: Object[]
}