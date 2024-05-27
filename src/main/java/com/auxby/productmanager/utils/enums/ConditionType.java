package com.auxby.productmanager.utils.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;

@Getter
public enum ConditionType {
    NEW("New"),
    USED("Used");

    private final String condition;

    ConditionType(String condition) {
        this.condition = condition;
    }

    @JsonCreator
    public static ConditionType getConditionType(String value) {
        return Arrays.stream(ConditionType.values())
                .toList()
                .stream()
                .filter(v -> v.getCondition().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Condition type not found for value:" + value));
    }


    @Override
    public String toString() {
        return condition;
    }
}
