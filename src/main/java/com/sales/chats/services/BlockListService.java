package com.sales.chats.services;


import com.sales.chats.repositories.BlockListHbRepository;
import com.sales.chats.repositories.BlockListRepository;
import com.sales.claims.AuthUser;
import com.sales.entities.BlockedUser;
import com.sales.entities.User;
import com.sales.exceptions.NotFoundException;
import com.sales.global.ResponseMessages;
import com.sales.utils.Utils;
import com.sales.wholesaler.repository.WholesaleUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockListService {

    private static final Logger logger = LoggerFactory.getLogger(BlockListService.class);
    private final WholesaleUserRepository wholesaleUserRepository;
    private final BlockListRepository blockListRepository;
    private final BlockListHbRepository blockListHbRepository;

    public BlockedUser addAUserInBlockList(AuthUser blockingBy, String blockedUserSlug) {
        logger.debug("Starting addAUserInBlockList method");
        User blockedUser = wholesaleUserRepository.findUserBySlug(blockedUserSlug);
        if (blockedUser == null) {
            logger.error("Blocked user not exists");
            throw new NotFoundException(ResponseMessages.BLOCKED_USER_NOT_EXISTS);
        }
        BlockedUser blockList = BlockedUser.builder()
                .userId(blockingBy.getId())
                .blockedUser(blockedUser)
                .createdAt(Utils.getCurrentMillis())
                .build();
        BlockedUser savedBlockList = blockListRepository.save(blockList); // Create operation
        logger.debug("Completed addAUserInBlockList method");
        return savedBlockList;
    }

    public boolean removeUserFromBlockList(Integer userId, String recipient) {
        User receiver = wholesaleUserRepository.findUserBySlug(recipient);
        return blockListHbRepository.deleteUserFromBlockList(userId, receiver);
    }


    public boolean isReceiverBlockedBySender(AuthUser loggedUser, User receiver) {
        logger.debug("Starting isReceiverBlockedBySender method the loggedUser : {} and the receiver : {} ", loggedUser, receiver);
        BlockedUser blockedUser = blockListRepository.findByUserIdAndBlockedUser(loggedUser.getId(), receiver);
        boolean exists = blockedUser != null;
        logger.debug("Completed isReceiverBlockedBySender method returning : {}", exists);
        return exists;
    }


    public boolean isSenderBlockedByReceiver(AuthUser loggedUser, User receiver) {
        logger.debug("Starting isSenderBlockedGyReceiver method");
        if (receiver == null) {
            logger.debug("Receiver is null, returning false");
            return false;
        }
        BlockedUser blockedUser = blockListRepository.findByUserIdAndBlockedUser(receiver.getId(), User.builder().id(loggedUser.getId()).build());
        boolean exists = blockedUser != null;
        logger.debug("Completed isSenderBlockedGyReceiver method");
        return exists;
    }

}
