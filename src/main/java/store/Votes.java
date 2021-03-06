package store;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Votes {
    public static final int CARPET_COUNT = 12;
    private static final int START_SCORE = 1000;

    private final int[] playedPerIndex;
    private final int[] scorePerIndex;
    private final int[] votesPerIndex;
    private final VotesRepository votesRepository;
    private final Executor executor;

    public Votes() {
        this.votesRepository = createVotesRepository();
        this.playedPerIndex = new int[CARPET_COUNT];
        this.scorePerIndex = new int[CARPET_COUNT];
        this.votesPerIndex = new int[CARPET_COUNT];
        this.executor = Executors.newSingleThreadExecutor();

        Arrays.fill(playedPerIndex, 0);
        Arrays.fill(scorePerIndex, START_SCORE);
        Arrays.fill(votesPerIndex, 0);

        votesRepository.refresh(this::computeVote);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                executor.execute(() -> votesRepository.refresh(Votes.this::computeVote));
            }
        }, 5000, 5000);
    }

    private VotesRepository createVotesRepository() {
        if ("true".equals(System.getenv("DATASTORE"))) {
            return new DataStoreRepository();
        } else {
            return new InMemoryRepository();
        }
    }

    public void vote(int winner, int looser) {
        computeVote(winner, looser);
        executor.execute(() -> votesRepository.vote(winner, looser));
    }

    public synchronized int score(int index) {
        return scorePerIndex[index];
    }

    public synchronized int votes(int index) {
        return votesPerIndex[index];
    }

    synchronized void computeVote(int winner, int looser) {
        int score1 = scorePerIndex[winner];
        int score2 = scorePerIndex[looser];
        int d = Math.min(400, Math.abs(score1 - score2));
        float p = 1f / (1f + (float) Math.pow(10, -d / 400f));
        int k1 = k(score1, ++playedPerIndex[winner]);
        int k2 = k(score2, ++playedPerIndex[looser]);

        int r1 = Math.round(score1 + (k1 * (1 - p)));
        int r2 = Math.round(score2 + (k2 * (0 - p)));

        votesPerIndex[winner]++;
        scorePerIndex[winner] = r1;
        scorePerIndex[looser] = r2;
    }

    private int k(int score, int played) {
        return played < 30 ? 25 : score < 2400 ? 15 : 10;
    }
}
