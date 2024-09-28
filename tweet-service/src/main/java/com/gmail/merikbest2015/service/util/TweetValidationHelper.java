package com.gmail.merikbest2015.service.util;

import com.gmail.merikbest2015.commons.constants.ErrorMessage;
import com.gmail.merikbest2015.commons.exception.ApiRequestException;
import com.gmail.merikbest2015.constants.TweetErrorMessage;
import com.gmail.merikbest2015.model.Tweet;
import com.gmail.merikbest2015.model.User;
import com.gmail.merikbest2015.repository.TweetRepository;
import com.gmail.merikbest2015.service.UserService;
import com.gmail.merikbest2015.commons.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TweetValidationHelper {

    private final TweetRepository tweetRepository;
    private final UserService userService;

    public Tweet checkValidTweet(Long tweetId) {
        Tweet tweet = tweetRepository.getTweetById(tweetId, Tweet.class)
                .orElseThrow(() -> new ApiRequestException(TweetErrorMessage.TWEET_NOT_FOUND, HttpStatus.NOT_FOUND));
        validateTweet(tweet.isDeleted(), tweet.getAuthor().getId());
        return tweet;
    }

    public void validateTweet(boolean isDeleted, Long tweetAuthorId) {
        if (isDeleted) {
            throw new ApiRequestException(TweetErrorMessage.TWEET_DELETED, HttpStatus.BAD_REQUEST);
        }
        checkIsValidUserProfile(tweetAuthorId);
    }

    public User validateUserProfile(Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ApiRequestException(String.format(ErrorMessage.USER_ID_NOT_FOUND, userId), HttpStatus.NOT_FOUND));
        checkIsValidUserProfile(userId);
        return user;
    }

    public void checkIsValidUserProfile(Long userId) {
        Long authUserId = AuthUtil.getAuthenticatedUserId();

        if (!userId.equals(authUserId)) {
            if (userService.isUserHavePrivateProfile(userId)) {
                throw new ApiRequestException(ErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            if (userService.isMyProfileBlockedByUser(userId)) {
                throw new ApiRequestException(ErrorMessage.USER_PROFILE_BLOCKED, HttpStatus.BAD_REQUEST);
            }
        }
    }

    public void checkTweetTextLength(String text) {
        if (text.length() == 0 || text.length() > 280) {
            throw new ApiRequestException(TweetErrorMessage.INCORRECT_TWEET_TEXT_LENGTH, HttpStatus.BAD_REQUEST);
        }
    }
}
