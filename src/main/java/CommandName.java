enum CommandName {
    exit,
    echo,
    type,
    pwd,
    cd;

    static CommandName of(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}