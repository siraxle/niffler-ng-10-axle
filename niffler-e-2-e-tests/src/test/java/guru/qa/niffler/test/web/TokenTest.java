package guru.qa.niffler.test.web;

public class TokenTest {

    public static void main(String[] args) {
        String token = System.getenv("GITHUB_TOKEN");
        System.out.println(token);
        System.out.println("Token: " + (token != null ? "SET" : "NOT SET"));
    }

}
