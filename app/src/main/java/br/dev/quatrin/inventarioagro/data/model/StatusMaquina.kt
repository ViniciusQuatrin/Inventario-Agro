package br.dev.quatrin.inventarioagro.data.model

enum class StatusMaquina(val codigo: Char, val descricao: String) {
    DISPONIVEL('D', "Disponível"),
    NEGOCIACAO('N', "Em Negociação"),
    RESERVADA('R', "Reservada"),
    VENDIDA('V', "Vendida")
}