package eu.metatools.wepwawet2

import java.util.*

/**
 * Repossessment table.
 */
class RepoTable : NavigableMap<Rev, MutableList<Id>> by TreeMap() {
    fun signOffIn(entityTable: EntityTable, rev: Rev) {
        val x = headMap(rev)
        for (repos in x.values)
            for (repo in repos)
                entityTable.remove(repo)

        x.clear()
    }
}