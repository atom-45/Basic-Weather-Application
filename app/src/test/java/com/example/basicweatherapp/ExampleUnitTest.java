package com.example.basicweatherapp;

import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testingTime(){
        LocalTime localTime = LocalTime.now();
        System.out.println();
    }

    @Test
    public void testingTimeSplit(){
        String str = "2022-07-22 00:00";
        System.out.println(str.split(" ")[1].substring(0,2));
    }

    @Test
    public void testingObservable(){
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        Observable<Integer> integerObservable = Observable.just(1,2,3,4);
        Observable<String> stringObservable = Observable.just("A","B","C");

        Observable<List<?>> concatBoth = Observable
                .concat(integerObservable.toList().toObservable(),
                        stringObservable.toList().toObservable());

        Disposable disposable = concatBoth
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {
                    if(!disposable1.isDisposed()){
                        compositeDisposable.add(disposable1);

                    }})
                .doOnError(throwable -> {
                    Log.e("TEST 1", "testingObservable: ", throwable);
                    compositeDisposable.dispose();})
                .doOnComplete(() -> {
                    Log.d("TEST 1", "testingObservable: completed ");
                    compositeDisposable.dispose();})
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        compositeDisposable.add(disposable);
        compositeDisposable.dispose();
    }
}