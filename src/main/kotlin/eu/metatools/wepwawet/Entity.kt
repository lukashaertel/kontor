package eu.metatools.wepwawet

interface Entity<I> {
    val parent: Wepwawet<I>
    val id: I
}