package util;

public interface IStringTransformer {

    IStringTransformer ECHO = new IStringTransformer() {
        @Override
        public String transform(final String str) {
            return str;
        }
    };

    String transform(final String str);
}
