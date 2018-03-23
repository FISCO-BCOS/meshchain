pragma solidity ^0.4.2;

import "LibTrieProof.sol";
import "LibVerifySign.sol";

contract Meshchain {

	/*
		errcode:
		10001:用户已存在
		10002:用户状态不正常
		10003:用户不存在
		10004:热点账户不存在
		10005:热点账户状态不正常
		10006:用户余额不足
		10007:冻结余额不合法
		10008:热点账户余额为0
		10009:没有可释放的金额
		10010:非热点账户
		10011:非影子户
		10012:trie proof验证失败

		10013:影子户不存在
		10014:影子户状态不正常
		10015:影子户已存在
		10016:公钥列表不存在
		10017:验证签名失败
		10018:金额非法
		10019:交易不存在
		10020:热点账户已存在
	*/

    struct UserInfo {
    	bytes32 uid;
    	uint availAssets;
    	uint unAvailAssets;
    	uint8 identity;//0:普通账户 1:热点账户
    	bytes32 name;//用户名字
    }

	struct Transfer {
		uint id;
		bytes32 from;
		bytes32 to;
		uint amount;
		uint8 status;//0:success 1:assets error 2:pending 3:verify error 4:endpoint receive error
	}

	struct Deposit {
		uint id;
		bytes32 uid;
		uint amount;
		uint8 status;
	}

    mapping(bytes32 => UserInfo) userMap;//uid as key
    mapping(bytes32 => bytes32) hotAccountMap;//name as key, uid as value
    mapping(uint => Transfer) transferMap;
    mapping(uint => Deposit) depositMap;

	uint incrTrans;
	uint incrDep;

    event retLog(int code);
    event depLog(uint id);
    event transferLog(uint id);

	event assetsLog(int code, uint availAssets, uint frozenAssets);

    //user register
    function register(bytes32 uid, uint assets, uint8 identity, bytes32 name) public returns(bool) {
        if (userMap[uid].uid != "") {
        	retLog(10001);
        	return false;
        }

		UserInfo memory user = UserInfo(uid, assets, 0, identity, name);

		if (identity == 1 && hotAccountMap[name] != "") {
			retLog(10020);
			return false;
		} else if (identity == 1 && hotAccountMap[name] == "") {
			hotAccountMap[name] = user.uid;
		}

		userMap[uid] = user;
        retLog(0);
        return true;
    }


	//充值
	function deposit(bytes32 uid, uint assets) public returns(bool) {
		if (userMap[uid].uid == "") {
			retLog(10003);
			return false;
		}

		UserInfo storage user = userMap[uid];
		user.availAssets += assets;

		userMap[uid] = user;

		incrDep += 1;
		Deposit memory dep = Deposit(incrDep, uid, assets, 0);
		depositMap[incrDep] = dep;
		retLog(0);
		depLog(incrDep);
		return true;
	}

    //transfer:from, to are in same chain
    function transferOneChain(bytes32 from, bytes32 to, uint assets) public returns(bool) {
        if (userMap[from].uid == "" || userMap[to].uid == "") {
        	retLog(10003);
	    	return false;
        }

        UserInfo storage fromUser = userMap[from];
        UserInfo storage toUser = userMap[to];

        incrTrans += 1;
        Transfer memory transfer = Transfer(incrTrans, from, to, assets, 0);

		if (fromUser.availAssets < assets) {
			transfer.status = 1;//from asset error
			transferMap[incrTrans] = transfer;
			retLog(10006);
			return false;
		}

		fromUser.availAssets -= assets;
        toUser.availAssets += assets;
        userMap[from] = fromUser;
        userMap[to] = toUser;

        transferMap[incrTrans] = transfer;
        retLog(0);
        transferLog(incrTrans);
        return true;
    }


	//transfer:from, to are not in same chain
	function transferInterChainByFrom(bytes32 from, bytes32 to, uint assets) public returns(bool) {
		if (userMap[from].uid == "") {
			retLog(10003);
			return false;
		}

		UserInfo storage fromUser = userMap[from];
		incrTrans += 1;
		Transfer memory transfer = Transfer(incrTrans, from, to, assets, 0);

		if (fromUser.availAssets < assets) {
			transfer.status = 1;//from asset error
			transferMap[incrTrans] = transfer;
			retLog(10006);
			return false;
		}

		fromUser.availAssets -= assets;
		fromUser.unAvailAssets += assets;

		transfer.status = 2;
		transferMap[incrTrans] = transfer;
		retLog(0);
		transferLog(incrTrans);
		return true;
	}


	function transferInterChainByTo(string merkleRoot, string merkleProofs, string key, string value, bytes32 from, bytes32 to, uint assets) public returns(bool) {
		if (userMap[to].uid == "") {
			retLog(10003);
			return false;
		}

		UserInfo storage toUser = userMap[to];
		incrTrans += 1;
		Transfer memory transfer = Transfer(incrTrans, from, to, assets, 0);

		uint ret = LibTrieProof.verifyProof(merkleRoot, merkleProofs, key, value);
		if (ret != 0) {
			transfer.status = 3;
			transferMap[incrTrans] = transfer;
			retLog(10012);
			return false;
		}

		toUser.availAssets += assets;
		transferMap[incrTrans] = transfer;
		retLog(0);
		transferLog(incrTrans);
		return true;
	}

	//transfer confirm when `to` receive assets successfully.
	function transferInterChainConfirm(uint transId) public returns(bool) {
		if (transferMap[transId].id == 0) {
			retLog(10019);
			return false;
		}

		Transfer storage transfer = transferMap[transId];
		UserInfo storage fromUser = userMap[transfer.from];
		fromUser.unAvailAssets -= transfer.amount;

		transfer.status = 0;
		transferMap[transId] = transfer;
		userMap[transfer.from] = fromUser;

		retLog(0);
		transferLog(transId);
		return true;
	}

	//transfer  cancel when `to` can not receive assets.
	function transferInterChainCancel(uint transId) public returns(bool) {
		if (transferMap[transId].id == 0) {
			retLog(10019);
			return false;
		}

		Transfer storage transfer = transferMap[transId];
		UserInfo storage fromUser = userMap[transfer.from];
		fromUser.unAvailAssets -= transfer.amount;
		fromUser.availAssets += transfer.amount;

		transfer.status = 4;
		transferMap[transId] = transfer;
		userMap[transfer.from] = fromUser;

		retLog(0);
		transferLog(transId);
		return true;
	}

	//验证签名 pubs,signs用";"分隔
	function verifySign(string hash, string pubs, string signs, string idxs) constant public returns(uint) {
		uint ret = LibVerifySign.verifySign(hash, pubs, signs, idxs);
    	if (ret != 0) {
    		return 10017;
    	}

		return ret;
	}

	function getUserInfo(bytes32 uid) constant public returns(uint, uint, uint, bytes32) {
		if (userMap[uid].uid == "") {
			return (0, 0, 0, "");
		}

    	UserInfo storage user = userMap[uid];
        return (user.availAssets, user.unAvailAssets, user.identity, user.name);
    }

    //get uid by hot account name
    function getHotAccoutByName(bytes32 name) constant public returns(bytes32, uint, uint, uint) {
    	if (hotAccountMap[name] == "") {
    		return ("", 0, 0, 0);
    	}

		bytes32 uid = hotAccountMap[name];
    	UserInfo storage user = userMap[uid];
    	return (user.uid, user.availAssets, user.unAvailAssets, user.identity);
    }

}
