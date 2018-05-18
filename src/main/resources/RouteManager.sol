pragma solidity ^0.4.4;

import "Set.sol";

contract RouteManager {
    uint public m_chainid;
    string public m_chainname;
    
    address[] public m_sets;
    string[] public m_setNames;
    mapping(bytes32 => mapping(string=>uint)) m_users;

    event registerRetLog(bool ok,uint set);
    
    function RouteManager(uint chainid,string chainname){
        m_chainid=chainid;
        m_chainname=chainname;
    }
    
    function registerSet(address set, string name) public {
        m_sets.push(set);
		m_setNames.push(name);
        Set(set).setId(m_sets.length-1);
    }
    
    function registerRoute(bytes32 user) public returns(bool) {
        if( m_users[user]["blocknumber"] > 0 ) {
			registerRetLog(true, m_users[user]["setid"]);
			return true;
		}

    	//check previous set if available
    	uint matchSet = m_sets.length;
    	for (uint i = 0; i < m_sets.length; i++) {
    		Set tmpSet=Set(m_sets[i]);
    		if (!tmpSet.isFull()) {
    			matchSet = i;
    			break;
    		}
    	}

		if (matchSet >= m_sets.length) {
			registerRetLog(false, 10086);
			return false;
		}

		Set set=Set(m_sets[matchSet]);
		bool register=set.registerRoute(user);
		if (register) {
       		m_users[user]["blocknumber"] = block.number;
            m_users[user]["setid"] = matchSet;
        	registerRetLog(true, m_users[user]["setid"]);
            return true;
        }
		registerRetLog(false, 10087);
        return false;
        
    }

    function getSetAddress(uint idx) constant public returns (bool, address) {
    	if (idx >= m_sets.length) {
    		return (false, 0x0000000000000000000000000000000000000000);
    	}

    	return (true, m_sets[idx]);
    }

    function getRoute(bytes32 user) constant public returns(bool,uint){
        if( m_users[user]["blocknumber"] > 0 )
            return (true,m_users[user]["setid"]);
            
        return (false,0);
    }
    
    function getSetsNum() constant public returns(uint){
        
        return m_sets.length;
    }
    
}
