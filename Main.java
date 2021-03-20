public class Main {
    public static void main(String[] args) {
        EventHandle eHandle = new EventHandle();
        eHandle.StartUp();

        boolean check = false;
        while (eHandle.Running()) {
            while (!check) {
                // prompts the user to login or register
                String choice = eHandle.login_register();
                if (choice.equals("s")) {
                    // sign-in
                    check = eHandle.promptSignIn();
                } else if (choice.equals("r")) {
                    // register (sign-up)
                    eHandle.promptSignUp();
                }
            }
            // prompt for further input
            // once logged in
            check = eHandle.menu_prompt();
        }
    }
}
