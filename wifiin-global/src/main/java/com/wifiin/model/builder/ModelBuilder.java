package com.wifiin.model.builder;

import com.wifiin.model.builder.exception.ModelBuiltException;

public abstract class ModelBuilder<T>{
    private boolean built=false;
    protected void built(){
        if(built){
            throw new ModelBuiltException("the method build() has been invoked, a new builder instance should be created instead");
        }
    }
    protected T build(){
        built=true;
        return null;
    }
}
