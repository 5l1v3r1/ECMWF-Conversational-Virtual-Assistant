package com._2horizon.cva.common.confluence.dto.space

import com.fasterxml.jackson.annotation.JsonProperty

data class Expandable(
    @JsonProperty("homepage")
    val homepage: String
)
