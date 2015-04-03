package main;

import net.codestory.http.annotations.Post;
import net.codestory.http.annotations.Prefix;
import store.Votes;

@Prefix("/votes")
public class VoteResource {
    private final Votes votes;

    public VoteResource(Votes votes) {
        this.votes = votes;
    }

    @Post(":winner/:looser")
    public void vote(int winner, int looser) {
        votes.vote(winner, looser);
    }
}
