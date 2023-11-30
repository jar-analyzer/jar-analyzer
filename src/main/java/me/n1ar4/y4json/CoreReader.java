package me.n1ar4.y4json;

import me.n1ar4.y4json.log.LogManager;
import me.n1ar4.y4json.log.Logger;
import me.n1ar4.y4json.token.*;

public class CoreReader implements JSONConst, State {
    private static final Logger logger = LogManager.getLogger();
    private final StringReader reader;
    private int state;

    public CoreReader(String text) {
        this.reader = new StringReader(text);
    }

    public JSONObject readJSON() {
        this.state = INIT_READ_OBJ;
        return this.readObject();
    }

    public JSONArray readJsonArray() {
        this.state = INIT_READ_ARRAY;
        return this.readArray();
    }

    public JSONObject readObject() {
        JSONObject object = new JSONObject();
        String curKey = null;
        while (true) {
            try {
                int c = this.reader.peek();
                if (c == EOF) {
                    logger.debug("read object eof");
                    break;
                }

                if (this.state == INIT_READ_OBJ) {
                    if (c == prefix) {
                        this.reader.read();
                        this.state = START_READ_OBJ;
                        this.readWhiteSpace();
                    } else {
                        logger.error("read error: init read obj error");
                        break;
                    }
                } else if (this.state == START_READ_OBJ) {
                    if (c == suffix) {
                        this.reader.read();
                        this.state = FINISH_READ_OBJ;
                        logger.debug("read object finish");
                        break;
                    } else {
                        this.state = INIT_READ_STRING;
                        String s = this.readString();
                        logger.debug("read string: " + s);
                        curKey = s;
                        this.state = FINISH_READ_STRING;
                    }
                } else if (this.state == FINISH_READ_STRING) {
                    this.readWhiteSpace();
                    this.readColon();
                    this.state = INIT_READ_VALUE;
                } else if (this.state == INIT_READ_VALUE) {
                    this.state = START_READ_VALUE;
                    Object value = this.readValue();
                    object.put(curKey, value);
                    logger.debug("read value: " + value);
                    this.state = FINISH_READ_VALUE;
                } else if (this.state == FINISH_READ_VALUE) {
                    if (c == comma) {
                        this.readComma();
                        this.readWhiteSpace();
                        this.state = INIT_READ_STRING;
                        String s = this.readString();
                        curKey = s;
                        logger.debug("read string: " + s);
                        this.state = FINISH_READ_STRING;
                    } else if (c == suffix) {
                        this.reader.read();
                        this.state = FINISH_READ_OBJ;
                        logger.debug("read object finish");
                        break;
                    }
                } else {
                    logger.error("unknown state");
                    break;
                }
            } catch (Exception ex) {
                logger.error("read error: " + ex);
                break;
            }
        }
        return object;
    }

    private Object readValue() {
        Object value;
        try {
            this.readWhiteSpace();
            int c = this.reader.peek();
            if (c == EOF) {
                logger.debug("read value eof");
                return null;
            }
            if (c == quotationMark) {
                this.state = INIT_READ_VALUE_STRING;
                String val = this.readString();
                value = new StringToken(val);
                this.readWhiteSpace();
            } else if (c == negative || ReaderHelper.isDigitAll(c)) {
                this.state = INIT_READ_NUMBER;
                String val = this.readNumber();
                value = new NumberToken(val);
                this.readWhiteSpace();
            } else if (c == prefix) {
                this.state = INIT_READ_OBJ;
                value = this.readObject();
                this.readWhiteSpace();
            } else if (c == arrayLeft) {
                this.state = INIT_READ_ARRAY;
                value = this.readArray();
                this.readWhiteSpace();
            } else if (c == 't') {
                this.state = INIT_READ_TRUE;
                this.readTrue();
                value = new TrueToken();
                this.readWhiteSpace();
            } else if (c == 'f') {
                this.state = INIT_READ_FALSE;
                this.readFalse();
                value = new FalseToken();
                this.readWhiteSpace();
            } else if (c == 'n') {
                this.state = INIT_READ_NULL;
                this.readNull();
                value = new NullToken();
                this.readWhiteSpace();
            } else {
                logger.error("read value error");
                return null;
            }
        } catch (Exception ex) {
            logger.error("read error: " + ex);
            return null;
        }
        return value;
    }

    private void readNull() {
        if (!(this.reader.read() == 'n' &&
                this.reader.read() == 'u' &&
                this.reader.read() == 'l' &&
                this.reader.read() == 'l')) {
            logger.error("read null error");
        }
    }

    private void readFalse() {
        if (!(this.reader.read() == 'f' &&
                this.reader.read() == 'a' &&
                this.reader.read() == 'l' &&
                this.reader.read() == 's' &&
                this.reader.read() == 'e')) {
            logger.error("read null error");
        }
    }

    private void readTrue() {
        if (!(this.reader.read() == 't' &&
                this.reader.read() == 'r' &&
                this.reader.read() == 'u' &&
                this.reader.read() == 'e')) {
            logger.error("read null error");
        }
    }

    private JSONArray readArray() {
        JSONArray array = new JSONArray();
        while (true) {
            try {
                int c = this.reader.peek();
                if (c == EOF) {
                    logger.debug("read object eof");
                    break;
                }
                if (this.state == INIT_READ_ARRAY) {
                    if (c == arrayLeft) {
                        this.state = START_READ_ARRAY;
                        this.reader.read();
                        this.readWhiteSpace();
                    } else {
                        logger.error("read array error");
                        break;
                    }
                } else if (this.state == START_READ_ARRAY) {
                    if (c == arrayRight) {
                        this.state = FINISH_READ_ARRAY;
                        this.reader.read();
                    } else {
                        this.state = INIT_READ_VALUE;
                    }
                } else if (this.state == INIT_READ_VALUE) {
                    this.state = START_READ_VALUE;
                    Object value = this.readValue();
                    array.add(value);
                    logger.debug("read value: " + value);
                    this.state = FINISH_READ_VALUE;
                } else if (this.state == FINISH_READ_VALUE) {
                    if (c == comma) {
                        this.readComma();
                        this.state = INIT_READ_VALUE;
                    } else if (c == arrayRight) {
                        this.state = FINISH_READ_ARRAY;
                        this.reader.read();
                        logger.debug("read array finish");
                        break;
                    }
                } else {
                    logger.error("unknown state");
                    break;
                }
            } catch (Exception ex) {
                logger.error("read error: " + ex);
                break;
            }
        }
        return array;
    }

    private String readNumber() {
        StringBuilder number = new StringBuilder();
        boolean isNegative = false;
        while (true) {
            try {
                int c = this.reader.peek();
                if (c == EOF) {
                    logger.debug("read number eof");
                    break;
                }
                if (this.state == INIT_READ_NUMBER) {
                    if (c == negative) {
                        if (!isNegative) {
                            this.state = READ_NUMBER_NEGATIVE;
                            isNegative = true;
                            number.append((char) c);
                            this.reader.read();
                        } else {
                            logger.error("read number negative error");
                            break;
                        }
                    } else if (ReaderHelper.isDigitAll(c)) {
                        this.state = READ_NUMBER_NORMAL;
                    } else {
                        logger.error("read number error");
                        break;
                    }
                } else if (this.state == READ_NUMBER_NEGATIVE ||
                        this.state == READ_NUMBER_NORMAL) {
                    if (c == '0') {
                        this.state = READ_NUMBER_ZERO;
                        this.reader.read();
                        number.append((char) c);
                    } else if (ReaderHelper.isDigit19(c)) {
                        this.state = READ_NUMBER_DIGIT_1_9;
                        this.reader.read();
                        number.append((char) c);
                    } else {
                        logger.error("read number error");
                        break;
                    }
                } else if (this.state == READ_NUMBER_DIGIT_1_9) {
                    if (ReaderHelper.isDigitAll(c)) {
                        number.append((char) c);
                        this.reader.read();
                    } else if (c == '.') {
                        this.state = READ_NUMBER_DOT;
                        number.append((char) c);
                        this.reader.read();
                    } else if (ReaderHelper.isDigitE(c)) {
                        this.state = READ_NUMBER_E;
                        number.append((char) c);
                        this.reader.read();
                    } else {
                        logger.debug("read number finish");
                        break;
                    }
                } else if (this.state == READ_NUMBER_ZERO) {
                    if (c == '.') {
                        this.state = READ_NUMBER_DOT;
                        number.append((char) c);
                        this.reader.read();
                    } else {
                        logger.debug("read number 0");
                        break;
                    }
                } else if (this.state == READ_NUMBER_DOT) {
                    this.state = READ_NUMBER_DOT_DIGIT;
                } else if (this.state == READ_NUMBER_DOT_DIGIT) {
                    if (ReaderHelper.isDigitAll(c)) {
                        number.append((char) c);
                        this.reader.read();
                    } else if (ReaderHelper.isDigitE(c)) {
                        this.state = READ_NUMBER_E;
                        number.append((char) c);
                        this.reader.read();
                    } else {
                        logger.debug("read number finish");
                        break;
                    }
                } else if (this.state == READ_NUMBER_E) {
                    if (ReaderHelper.isDigitEPlus(c)) {
                        this.state = READ_NUMBER_E_PLUS;
                        number.append((char) c);
                        this.reader.read();
                    } else {
                        logger.error("read number unknown state");
                        break;
                    }
                } else if (this.state == READ_NUMBER_E_PLUS) {
                    if (ReaderHelper.isDigitAll(c)) {
                        number.append((char) c);
                        this.reader.read();
                    } else {
                        logger.debug("read number finish");
                        break;
                    }
                } else {
                    logger.error("read number unknown state");
                    break;
                }
            } catch (Exception ex) {
                logger.error("read error: " + ex);
                break;
            }
        }
        return number.toString();
    }

    private void readComma() {
        try {
            int c = this.reader.peek();
            if (c == EOF) {
                logger.debug("read comma eof");
            }
            if (c == comma) {
                this.reader.read();
            }
        } catch (Exception ex) {
            logger.error("read error: " + ex);
        }
    }

    private void readColon() {
        try {
            int c = this.reader.peek();
            if (c == EOF) {
                logger.debug("read colon eof");
            }
            if (c == colon) {
                this.reader.read();
            }
        } catch (Exception ex) {
            logger.error("read error: " + ex);
        }
    }

    public void readWhiteSpace() {
        while (true) {
            try {
                int c = this.reader.peek();
                if (c == EOF) {
                    logger.debug("read white space eof");
                    break;
                }
                if (ReaderHelper.isWhite(c)) {
                    this.reader.read();
                    continue;
                }
                break;
            } catch (Exception ex) {
                logger.error("read error: " + ex);
                break;
            }
        }
    }

    @SuppressWarnings("all")
    public String readString() {
        boolean writeEscape = false;
        StringBuilder sb = new StringBuilder();
        while (true) {
            try {
                int c = this.reader.peek();
                if (c == EOF) {
                    logger.debug("read string eof");
                    break;
                }

                if (c == quotationMark) {
                    if (this.state == INIT_READ_STRING) {
                        this.state = START_READ_STRING;
                        this.reader.read();
                        continue;
                    } else if (this.state == START_READ_STRING) {
                        this.state = FINISH_READ_STRING;
                        logger.debug("read string finish");
                        this.reader.read();
                        break;
                    } else if (this.state == INIT_READ_VALUE_STRING) {
                        this.state = START_READ_VALUE_STRING;
                        this.reader.read();
                        continue;
                    } else if (this.state == START_READ_VALUE_STRING) {
                        this.state = FINISH_READ_VALUE_STRING;
                        logger.debug("read value string finish");
                        this.reader.read();
                        break;
                    }
                } else if (this.reader.peekPrev() == reverseSolidus) {
                    if (c == reverseSolidus) {
                        if (writeEscape) {
                            writeEscape = false;
                        } else {
                            sb.append(reverseSolidus);
                            writeEscape = true;
                        }
                        continue;
                    } else if (c == 'b') {
                        sb.append(backSpace);
                    } else if (c == 'f') {
                        sb.append(formFeed);
                    } else if (c == 'n') {
                        sb.append(lineFeed);
                    } else if (c == 'r') {
                        sb.append(carriageReturn);
                    } else if (c == 't') {
                        sb.append(horizontalTab);
                    } else if (c == quotationMark) {
                        sb.append(quotationMark);
                    } else if (c == 'u') {
                        String uni = this.readUnicode();
                        char unicode = (char) Integer.parseInt(uni, 16);
                        sb.append(unicode);
                    }
                } else {
                    this.reader.read();
                    sb.append((char) c);
                }
            } catch (Exception ex) {
                logger.error("read error: " + ex);
                break;
            }
        }
        return sb.toString();
    }

    public String readUnicode() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            try {
                int c = this.reader.read();
                if (c == EOF) {
                    logger.debug("read unicode eof");
                    break;
                }
                if (ReaderHelper.isHex(c)) {
                    sb.append((char) c);
                    continue;
                }
                break;
            } catch (Exception ex) {
                logger.error("read error: " + ex);
                break;
            }
        }
        return sb.toString();
    }
}
