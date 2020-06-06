package com._2horizon.cva.retrieval.event

import com._2horizon.cva.retrieval.confluence.dto.content.Content
import com._2horizon.cva.retrieval.confluence.dto.space.Space
import com._2horizon.cva.retrieval.copernicus.Datastore
import com._2horizon.cva.retrieval.copernicus.dto.ui.UiResource
import com._2horizon.cva.retrieval.ecmwf.publications.dto.EcmwfPublicationDTO
import com._2horizon.cva.retrieval.nlp.SignificantTerm
import com._2horizon.cva.retrieval.sitemap.Sitemap

/**
 * Created by Frank Lieber (liefra) on 2020-05-11.
 */
data class ConfluenceContentEvent( val spaceKey:String, val contentList: List<Content>)
data class ConfluenceSpacesEvent(val spacesList: List<Space>)
data class SitemapEvent(val sitemapsList: List<Sitemap>)
data class EcmwfPublicationEvent(val ecmwfPublicationDTO: EcmwfPublicationDTO)
data class CopernicusCatalogueReceivedEvent(val datastore:Datastore, val uiResources: List<UiResource>)
data class SignificantTermsReceivedEvent(val datastore: Datastore, val significantTerms: List<SignificantTerm>)


