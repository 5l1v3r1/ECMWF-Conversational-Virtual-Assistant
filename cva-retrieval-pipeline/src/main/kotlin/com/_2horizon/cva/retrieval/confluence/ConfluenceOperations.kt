package com._2horizon.cva.retrieval.confluence

import com._2horizon.cva.common.confluence.dto.content.Content
import com._2horizon.cva.common.confluence.dto.content.ContentResponse
import com._2horizon.cva.common.confluence.dto.pagechildren.PageChildrenResponse
import com._2horizon.cva.common.confluence.dto.space.SpacesResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable
import java.util.Optional

/**
 * Created by Frank Lieber (liefra) on 2020-05-09.
 */
@Client("https://confluence.ecmwf.int/rest/api")
@Retryable(attempts = "5", multiplier = "1.5")
interface ConfluenceOperations {

    @Get("/space?start={start}&limit={limit}&type={type}&expand=metadata.labels,description.view,description.plain,icon")
    fun spacesWithMetadataLabelsAndDescriptionAndIcon(
        limit: Int = 500,
        start: Int = 0,
        type: String = "global"
    ): Optional<SpacesResponse>

    @Get("/content?type=page&spaceKey={spaceKey}&start={start}&limit={limit}&expand=history,version,metadata.labels,body.view,body.storage")
    fun contentWithMetadataLabelsAndDescriptionAndIcon(
        spaceKey: String,
        limit: Int = 10,
        start: Int = 0
    ): Optional<ContentResponse>

    @Get("/content/{nodeId}?expand=history,version,metadata.labels,body.view,body.storage")
    fun contentByIdWithMetadataLabelsAndDescriptionAndIcon(
        nodeId: Long
    ): Optional<Content>

    @Get("/content/{contentId}/child?start={start}&limit={limit}&expand=page.children.page")
    fun contentWithChildPages(
        contentId: Long,
        limit: Int = 500,
        start: Int = 0
    ): PageChildrenResponse

    @Get("/content/{contentId}/child/comment?&start={start}&limit={limit}&expand=history,version,metadata.labels,body.view,body.storage")
    fun contentComments(
        contentId: Long,
        limit: Int = 200,
        start: Int = 0
    ): Optional<ContentResponse>
}
