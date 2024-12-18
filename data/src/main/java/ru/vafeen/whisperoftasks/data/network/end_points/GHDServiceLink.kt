package ru.vafeen.whisperoftasks.data.network.end_points

internal object GHDServiceLink {
    const val BASE_LINK = "https://api.github.com/"


    object EndPoint {
        const val LATEST_RELEASE_INFO =
            "repos/${RepoInfo.USER_NAME}/${RepoInfo.REPO_NAME}/releases/latest"
    }
}