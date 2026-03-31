package br.com.mechanic.account.enuns;

public enum TopicStatusEnum {
    OPEN,
    CLOSED,
    CANCELED,
    REVIEWED,
    /** Requisição enviada para pipeline de IA assíncrono; aguardando validação final. */
    AI_ANALYSIS_PENDING,
    /** Relatório de consolidação via IA persistido em {@code topic_ai_report} (tópicos com workflow). */
    AI_REPORT_READY
}
