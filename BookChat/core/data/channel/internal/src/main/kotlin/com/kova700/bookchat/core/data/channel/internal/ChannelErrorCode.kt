package com.kova700.bookchat.core.data.channel.internal

/**채팅방 정원이 꽉 찬 경우*/
internal const val RESPONSE_CODE_CHANNEL_IS_FULL = 4000400

/**채팅방이 폭파되어 찾을 수 없는 경우*/
internal const val RESPONSE_CODE_CHANNEL_IS_EXPLODED = 4040400

/** 이미 참여한 경우 */
internal const val RESPONSE_CODE_ALREADY_ENTERED_CHANNEL = 4000501

/** 강퇴 당한 경우 */
internal const val RESPONSE_CODE_CHANNEL_IS_BANNED = 4030400

/** 채팅방 참가자를 찾을 수 없는 경우 (클라이언트가 채팅방에 존재하지 않는 경우) */
internal const val RESPONSE_CODE_CHANNEL_PARTICIPANT_NOT_FOUND = 4040500
