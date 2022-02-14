package moe.caa.multilogin.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<V1, V2> {
    private final V1 value1;
    private final V2 value2;
}
