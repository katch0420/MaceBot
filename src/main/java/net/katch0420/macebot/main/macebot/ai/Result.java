package net.katch0420.macebot.main.macebot.ai;

import net.katch0420.macebot.main.macebot.ai.action.Action;

public record Result(Status s, Action<?> n) {
    public enum Status {SUCCESS, MAY_DO, MUST_DO, FAIL, TICK}
    public static Result success(){ return new Result(Status.SUCCESS, null); }
    public static Result fail(){ return new Result(Status.FAIL, null); }
    public static Result mayDo(Action<?> n){ return new Result(Status.MAY_DO, n); }
    public static Result mustDo(Action<?> n){ return new Result(Status.MUST_DO, n); }
    public static Result tick(){ return new Result(Status.TICK, null); }
}
