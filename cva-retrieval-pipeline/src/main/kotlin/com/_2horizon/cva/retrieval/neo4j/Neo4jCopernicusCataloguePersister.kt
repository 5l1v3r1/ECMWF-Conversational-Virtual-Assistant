package com._2horizon.cva.retrieval.neo4j

import com._2horizon.cva.retrieval.copernicus.dto.ui.UiResource
import com._2horizon.cva.retrieval.event.CopernicusCatalogueReceivedEvent
import com._2horizon.cva.retrieval.neo4j.domain.Application
import com._2horizon.cva.retrieval.neo4j.domain.Dataset
import com._2horizon.cva.retrieval.neo4j.domain.DatasetDomain
import com._2horizon.cva.retrieval.neo4j.domain.DatasetProvider
import com._2horizon.cva.retrieval.neo4j.domain.DatasetTerms
import com._2horizon.cva.retrieval.neo4j.domain.Documentation
import com._2horizon.cva.retrieval.neo4j.domain.Lineage
import com._2horizon.cva.retrieval.neo4j.domain.ParameterFamily
import com._2horizon.cva.retrieval.neo4j.domain.ProductType
import com._2horizon.cva.retrieval.neo4j.domain.Sector
import com._2horizon.cva.retrieval.neo4j.domain.SpatialCoverage
import com._2horizon.cva.retrieval.neo4j.domain.TemporalCoverage
import com._2horizon.cva.retrieval.neo4j.repo.DatasetRepository
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import org.neo4j.ogm.session.SessionFactory
import org.slf4j.LoggerFactory
import javax.inject.Singleton

/**
 * Created by Frank Lieber (liefra) on 2020-06-01.
 */
@Requirements(
    Requires(beans = [SessionFactory::class]),
    Requires(property = "app.feature.ingest-pipeline.neo4j-ingest-enabled", value = "true")
)
@Singleton
open class Neo4jCopernicusCataloguePersister(
    private val datasetRepository: DatasetRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    open fun cdsCatalogueReceivedEvent(copernicusCatalogueReceivedEvent: CopernicusCatalogueReceivedEvent) {
        log.info("Neo4j CdsCatalogueReceivedEvent received")

        val uiResources = copernicusCatalogueReceivedEvent.uiResources

        val cdsKeywords = uiResources.flatMap { it.cdsKeywords }.sorted().map {
            val s = it.split(":")
            check(s.size == 2)
            val label = s.first()
            val value = s.last()
            Pair(label, value)
        }.groupBy { it.first }

        val methods = uiResources.map { it.method }.toSet()


        uiResources.forEach { r: UiResource ->

            val domains: Set<DatasetDomain> = extractDatasetDomains(r.cdsKeywords)
            val providers: Set<DatasetProvider> = extractDatasetProviders(r.cdsKeywords)
            val parameterFamilies: Set<ParameterFamily> = extractParameterFamilies(r.cdsKeywords)
            val productTypes: Set<ProductType> = extractProductTypes(r.cdsKeywords)
            val sectors: Set<Sector> = extractSectors(r.cdsKeywords)
            val spatialCoverages: Set<SpatialCoverage> = extractSpatialCoverages(r.cdsKeywords)
            val temporalCoverages: Set<TemporalCoverage> = extractTemporalCoverages(r.cdsKeywords)

            val terms = r.terms.map { DatasetTerms(it) }.toSet()
            val docs = r.externalLinks.map { externalLink ->

                val isWiki =
                    externalLink.url.startsWith("https://software.ecmwf.int/wiki") || externalLink.url.startsWith("https://confluence.ecmwf.int")

                Documentation(
                    url = externalLink.url,
                    title = externalLink.name,
                    description = externalLink.description,
                    isWiki = isWiki
                )

            }.toSet()

            if (r.type == "dataset") {
                val dataset = Dataset(
                    id = r.id,
                    name = r.name,
                    title = r.title,
                    downloadable = r.downloadable,
                    type = r.type,
                    lineage = r.lineage?.let { Lineage(it) },
                    publicationDate = r.publicationDate,
                    domains = domains,
                    providers = providers,
                    parameterFamilies = parameterFamilies,
                    productTypes = productTypes,
                    sectors = sectors,
                    spatialCoverages = spatialCoverages,
                    temporalCoverages = temporalCoverages,
                    terms = terms,
                    docs = docs
                )
                datasetRepository.save(dataset)
            } else if (r.type == "application") {
                val application = Application(
                    id = r.id,
                    name = r.name,
                    title = r.title,
                    downloadable = r.downloadable,
                    type = r.type,
                    lineage = r.lineage,
                    publicationDate = r.publicationDate,
                    domains = domains,
                    providers = providers,
                    parameterFamilies = parameterFamilies,
                    productTypes = productTypes,
                    sectors = sectors,
                    spatialCoverages = spatialCoverages,
                    temporalCoverages = temporalCoverages,
                    terms = terms,
                    docs = docs
                )
                datasetRepository.save(application)
            }

        }

        // handle relationships
        uiResources.forEach { r: UiResource ->

            if (r.type == "dataset") {
                val dataset = datasetRepository.load<Dataset>(r.id)

                val relatedDatasets = r.relatedResources.map { relatedResource ->
                    val rel = datasetRepository.load<Dataset>(uiResources.find { it.name == relatedResource.name }!!.id)
                    rel
                }.toSet()

                if (relatedDatasets.isNotEmpty())
                    datasetRepository.save(dataset.copy(relatedDatasets = relatedDatasets))
            }

            if (r.type == "application") {
                val application = datasetRepository.load<Application>(r.id)

                val relatedApplications = r.relatedResources.map { relatedResource ->
                    val rel = datasetRepository.load<Application>(uiResources.find { it.name == relatedResource.name }!!.id)
                    rel
                }.toSet()

                if (relatedApplications.isNotEmpty())
                    datasetRepository.save(application.copy(relatedApplications = relatedApplications))
            }
        }

        log.debug("DONE with Neo4j CdsCatalogueReceivedEvent received")
    }

    private fun extractTemporalCoverages(cdsKeywords: List<String>): Set<TemporalCoverage> {
        val extractKeyword = extractKeywords(cdsKeywords, "Temporal Coverage")
        return if (extractKeyword != null) {
            setOf(TemporalCoverage(extractKeyword))
        } else {
            emptySet()
        }
    }

    private fun extractSpatialCoverages(cdsKeywords: List<String>): Set<SpatialCoverage> {
        val extractKeyword = extractKeywords(cdsKeywords, "Spatial Coverage")
        return if (extractKeyword != null) {
            setOf(SpatialCoverage(extractKeyword))
        } else {
            emptySet()
        }
    }

    private fun extractSectors(cdsKeywords: List<String>): Set<Sector> {
        val extractKeyword = extractKeywords(cdsKeywords, "Sector")
        return if (extractKeyword != null) {
            setOf(Sector(extractKeyword))
        } else {
            emptySet()
        }
    }

    private fun extractProductTypes(cdsKeywords: List<String>): Set<ProductType> {
        val extractKeyword = extractKeywords(cdsKeywords, "Product type")
        return if (extractKeyword != null) {
            setOf(ProductType(extractKeyword))
        } else {
            emptySet()
        }
    }

    private fun extractParameterFamilies(cdsKeywords: List<String>): Set<ParameterFamily> {
        val extractKeyword = extractKeywords(cdsKeywords, "Parameter family")
        return if (extractKeyword != null) {
            setOf(ParameterFamily(extractKeyword))
        } else {
            emptySet()
        }
    }

    private fun extractDatasetProviders(cdsKeywords: List<String>): Set<DatasetProvider> {
        val extractKeyword = extractKeywords(cdsKeywords, "Provider")
        return if (extractKeyword != null) {
            setOf(DatasetProvider(extractKeyword))
        } else {
            emptySet()
        }
    }

    private fun extractDatasetDomains(cdsKeywords: List<String>): Set<DatasetDomain> {
        val extractKeyword = extractKeywords(cdsKeywords, "Variable domain")
        return if (extractKeyword != null) {
            setOf(DatasetDomain(extractKeyword))
        } else {
            emptySet()
        }
    }

    private fun extractKeywords(cdsKeywords: List<String>, filterKey: String): String? {
        return cdsKeywords.map(::splitCdsKeyword).toMap()[filterKey.toLowerCase()]
    }

    private fun splitCdsKeyword(cdsKeyword: String): Pair<String, String> {
        val s = cdsKeyword.split(":")
        check(s.size == 2)
        val label = s.first().toLowerCase()

        // There is a spelling mistake for certain datasets
        val correctedLabel = if (label == "variale domain") {
            "variable domain"
        } else {
            label
        }

        val value = s.last()
        return Pair(correctedLabel, value)
    }
}

