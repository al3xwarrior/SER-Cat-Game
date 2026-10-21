package Utils;

// enum for what controls the player will use, just the names of them

import Engine.Key;

// this literally only exists to make life a little easier but its worth it in the end!
public enum PlayerControls {
    JUMP, LEFT, RIGHT, CROUCH, DASH, POUNCE, DIVE;

    static public Key jumpControl = Key.W;
    static public Key leftControl = Key.A;
    static public Key rightControl = Key.D;
    static public Key crouchControl = Key.S;

    static public Key dashControl = Key.J;
    static public Key pounceControl = Key.K;
    static public Key diveControl = Key.L;

    static public Key getCrouchControl() {
        return crouchControl;
    }

    static public Key getDashControl() {
        return dashControl;
    }

    static public Key getDiveControl() {
        return diveControl;
    }

    static public Key getJumpControl() {
        return jumpControl;
    }

    static public Key getLeftControl() {
        return leftControl;
    }

    static public Key getPounceControl() {
        return pounceControl;
    }

    static public Key getRightControl() {
        return rightControl;
    }

    public static void setCrouchControl(Key crouchControl) {
        PlayerControls.crouchControl = crouchControl;
    }

    public static void setDashControl(Key dashControl) {
        PlayerControls.dashControl = dashControl;
    }

    public static void setDiveControl(Key diveControl) {
        PlayerControls.diveControl = diveControl;
    }

    public static void setJumpControl(Key jumpControl) {
        IO.println("ive changed my jump control");
        PlayerControls.jumpControl = jumpControl;
    }

    public static void setLeftControl(Key leftControl) {
        PlayerControls.leftControl = leftControl;
    }

    public static void setPounceControl(Key pounceControl) {
        PlayerControls.pounceControl = pounceControl;
    }

    public static void setRightControl(Key rightControl) {
        PlayerControls.rightControl = rightControl;
    }
}
