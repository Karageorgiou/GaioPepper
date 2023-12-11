package gr.ntua.metal.gaiopepper.util.fsm;


import androidx.annotation.Nullable;

public interface IState {

    void enter(@Nullable Object data);

    void exit();

    String getName();
}
