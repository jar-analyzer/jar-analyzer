package me.n1ar4.y4json;

/**
 * 状态
 */
public interface State {
    // ######################## OBJECT ########################
    int INIT_READ_OBJ = 1;
    int START_READ_OBJ = 2;
    int FINISH_READ_OBJ = 3;
    // ######################## STRING ########################
    int INIT_READ_STRING = 4;
    int START_READ_STRING = 5;
    int FINISH_READ_STRING = 6;
    int INIT_READ_VALUE = 7;
    int START_READ_VALUE = 8;
    int FINISH_READ_VALUE = 9;
    int INIT_READ_VALUE_STRING = 10;
    int START_READ_VALUE_STRING = 11;
    int FINISH_READ_VALUE_STRING = 12;
    // ######################## NUMBER ########################
    int INIT_READ_NUMBER = 13;
    int READ_NUMBER_NEGATIVE = 16;
    int READ_NUMBER_NORMAL = 17;
    int READ_NUMBER_ZERO = 18;
    int READ_NUMBER_DIGIT_1_9 = 19;
    int READ_NUMBER_DOT = 21;
    int READ_NUMBER_DOT_DIGIT = 22;
    int READ_NUMBER_E = 23;
    int READ_NUMBER_E_PLUS = 24;
    // ######################## VALUE ########################
    int INIT_READ_ARRAY = 25;
    int INIT_READ_TRUE = 26;
    int INIT_READ_FALSE = 27;
    int INIT_READ_NULL = 28;
    // ######################## ARRAY ########################
    int START_READ_ARRAY = 29;
    int FINISH_READ_ARRAY = 30;
}
