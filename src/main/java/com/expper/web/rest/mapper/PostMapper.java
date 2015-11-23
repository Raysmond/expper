package com.expper.web.rest.mapper;

import com.expper.domain.*;
import com.expper.web.rest.dto.PostDTO;
import org.mapstruct.*;


/**
 * Mapper for the entity Post and its DTO PostDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PostMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userName")
    PostDTO postToPostDTO(Post post);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userName")
    @Mapping(target = "content", ignore = true)
    PostDTO postToSimplePostDTO(Post post);

    @Mapping(source = "userId", target = "user")
    Post postDTOToPost(PostDTO postDTO);

    default User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }
}
