package com._2horizon.cva.retrieval.confluence.dto.content


import com.fasterxml.jackson.annotation.JsonProperty

data class CreatedBy(
    @JsonProperty("displayName")
    val displayName: String,
    @JsonProperty("_links")
    val userLinks: UserLinks,
    @JsonProperty("profilePicture")
    val profilePicture: ProfilePicture,
    @JsonProperty("type")
    val type: String,
    @JsonProperty("userKey")
    val userKey: String,
    @JsonProperty("username")
    val username: String
)