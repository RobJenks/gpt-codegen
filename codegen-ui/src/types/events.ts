export type ContextEntry = {
    role: string,
    content: string
}

export type SessionState = {
    id: string,
    currentIntermediateModelData: string,
    currentBpmnData: string,
}

export type _SessionState = {
    id: string,
    executionContext: string,
    events: ContextEntry[],
    totalTokensUsed: int,
    estimatedCompressedTokenSize: int,
    estimatedUncompressedTokenSize: int,
    lastPrompt: string,
    lastResponse: string,
    validOutput: boolean,
    validationErrors: string[],
    iterationsRequired: int,
    currentTemperature: number,
    transformedContent: string
}

export type GenerateResponseData = {
    
}

export type Data = {
    key: string,
    offset: int,
    value: Object[]
}