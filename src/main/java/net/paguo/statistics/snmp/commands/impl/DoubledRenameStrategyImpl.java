package net.paguo.statistics.snmp.commands.impl;

import net.paguo.statistics.snmp.commands.RenameStrategy;

/**
 * @author Reyentenko
 */
public class DoubledRenameStrategyImpl implements RenameStrategy {
    public String renameInterface(String ifaceName, Long ifaceId) {
        return ifaceName
                       + "-ix" + ifaceId;
    }
}
