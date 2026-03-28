package com.sales.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomUserDto {
     Long id;
     String userSlug;
     String roomSlug;
}
