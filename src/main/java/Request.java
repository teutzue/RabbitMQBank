
public class Request {

    private int ssn;
    private int creditScore;
    private float loan;
    private float duration;

    public Request(int ssn, int creditScore, float loan, float duration) {
        this.ssn = ssn;
        this.creditScore = creditScore;
        this.loan = loan;
        this.duration = duration;
    }


    public int getSsn() {
        return ssn;
    }

    public void setSsn(int ssn) {
        this.ssn = ssn;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public float getLoan() {
        return loan;
    }

    public void setLoan(float loan) {
        this.loan = loan;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }
}
