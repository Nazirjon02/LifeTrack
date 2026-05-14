package tj.mahram.lifetrack

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform