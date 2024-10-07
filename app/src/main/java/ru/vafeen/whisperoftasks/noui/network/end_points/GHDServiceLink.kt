package ru.vafeen.whisperoftasks.network.end_points

import ru.vafeen.whisperoftasks.noui.network.end_points.RepoInfo

object GHDServiceLink {
    const val BASE_LINK = "https://api.github.com/"


    object EndPoint {
        const val LATEST_RELEASE_INFO =
            "repos/${RepoInfo.USER_NAME}/${RepoInfo.REPO_NAME}/releases/latest"
    }
}