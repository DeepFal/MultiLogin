package moe.caa.multilogin.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModifyThere<V1, V2, V3> {
    private V1 value1;
    private V2 value2;
    private V3 value3;
}
