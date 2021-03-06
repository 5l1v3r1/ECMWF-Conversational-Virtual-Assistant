package com._2horizon.cva.common.dialogflow.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RichContentAccordionItem(
    @JsonProperty("title")
    val title: String,

    @JsonProperty("subtitle")
    val subtitle: String? = null,

    @JsonProperty("text")
    val text: String,

    @JsonProperty("image")
    val image: RichContentImage? = null,

    @JsonProperty("type")
    override val type: String = "accordion"
) : RichContentItem
