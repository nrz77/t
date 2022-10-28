/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.security.SecureRandom;
import java.nio.*;
import java.nio.charset.Charset;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.tron.api.GrpcAPI.AccountNetMessage;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.api.GrpcAPI.TransactionList;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.JsonFormat.ParseException;
import org.tron.protos.Contract.AccountCreateContract;
import org.tron.protos.Contract.AccountUpdateContract;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Contract.AssetIssueContract.FrozenSupply;
import org.tron.protos.Contract.DeployContract;
import org.tron.protos.Contract.FreezeBalanceContract;
import org.tron.protos.Contract.ParticipateAssetIssueContract;
import org.tron.protos.Contract.TransferAssetContract;
import org.tron.protos.Contract.TransferContract;
import org.tron.protos.Contract.UnfreezeAssetContract;
import org.tron.protos.Contract.UnfreezeBalanceContract;
import org.tron.protos.Contract.UpdateAssetContract;
import org.tron.protos.Contract.VoteAssetContract;
import org.tron.protos.Contract.VoteWitnessContract;
import org.tron.protos.Contract.WithdrawBalanceContract;
import org.tron.protos.Contract.WitnessCreateContract;
import org.tron.protos.Contract.WitnessUpdateContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Account.Frozen;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.Vote;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.BlockHeader;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract;
import org.tron.protos.Protocol.Transaction.Result;
import org.tron.protos.Protocol.Witness;

public class Utils {

  private static SecureRandom random = new SecureRandom();

  public static SecureRandom getRandom() {
    return random;
  }

  public static byte[] getBytes(char[] chars) {
    Charset cs = Charset.forName("UTF-8");
    CharBuffer cb = CharBuffer.allocate(chars.length);
    cb.put(chars);
    cb.flip();
    ByteBuffer bb = cs.encode(cb);

    return bb.array();
  }

  private char[] getChars(byte[] bytes) {
    Charset cs = Charset.forName("UTF-8");
    ByteBuffer bb = ByteBuffer.allocate(bytes.length);
    bb.put(bytes);
    bb.flip();
    CharBuffer cb = cs.decode(bb);

    return cb.array();
  }

  /**
   * yyyy-MM-dd
   */
  public static Date strToDateLong(String strDate) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    ParsePosition pos = new ParsePosition(0);
    Date strtodate = formatter.parse(strDate, pos);
    return strtodate;
  }

  public static String printAccount(Account account) {
    String result = "";
    result += "address: ";
    result += Wallet.encode58Check(account.getAddress().toByteArray());
    result += "\n";
    if (account.getAccountName() != null && account.getAccountName().size() != 0) {
      result += "account_name: ";
      result += new String(account.getAccountName().toByteArray(), Charset.forName("UTF-8"));
      result += "\n";
    }
    result += "type: ";
    result += account.getTypeValue();
    result += "\n";
    result += "balance: ";
    result += account.getBalance();
    result += "\n";
    if (account.getFrozenCount() > 0) {
      for (Frozen frozen : account.getFrozenList()) {
        result += "frozen";
        result += "\n";
        result += "{";
        result += "\n";
        result += "  frozen_balance: ";
        result += frozen.getFrozenBalance();
        result += "\n";
        result += "  expire_time: ";
        result += new Date(frozen.getExpireTime());
        result += "\n";
        result += "}";
        result += "\n";
      }
    }
    result += "free_net_usage: ";
    result += account.getFreeNetUsage();
    result += "\n";
    result += "net_usage: ";
    result += account.getNetUsage();
    result += "\n";
    if (account.getCreateTime() != 0) {
      result += "create_time: ";
      result += new Date(account.getCreateTime());
      result += "\n";
    }
    if (account.getVotesCount() > 0) {
      for (Vote vote : account.getVotesList()) {
        result += "votes";
        result += "\n";
        result += "{";
        result += "\n";
        result += "  vote_address: ";
        result += Wallet.encode58Check(vote.getVoteAddress().toByteArray());
        result += "\n";
        result += "  vote_count: ";
        result += vote.getVoteCount();
        result += "\n";
        result += "}";
        result += "\n";
      }
    }
    if (account.getAssetCount() > 0) {
      for (String name : account.getAssetMap().keySet()) {
        result += "asset";
        result += "\n";
        result += "{";
        result += "\n";
        result += "  name: ";
        result += name;
        result += "\n";
        result += "  balance: ";
        result += account.getAssetMap().get(name);
        result += "\n";
        result += "  latest_asset_operation_time: ";
        result += account.getLatestAssetOperationTimeMap().get(name);
        result += "\n";
        result += "  free_asset_net_usage: ";
        result += account.getFreeAssetNetUsageMap().get(name);
        result += "\n";
        result += "}";
        result += "\n";
      }
    }
    if (account.getFrozenSupplyCount() > 0) {
      for (Frozen frozen : account.getFrozenSupplyList()) {
        result += "frozen_supply";
        result += "\n";
        result += "{";
        result += "\n";
        result += "  amount: ";
        result += frozen.getFrozenBalance();
        result += "\n";
        result += "  expire_time: ";
        result += new Date(frozen.getExpireTime());
        result += "\n";
        result += "}";
        result += "\n";
      }
    }
    result += "latest_opration_time: ";
    result += new Date(account.getLatestOprationTime());
    result += "\n";

    result += "latest_consume_time: ";
    result += account.getLatestConsumeTime();
    result += "\n";

    result += "latest_consume_free_time: ";
    result += account.getLatestConsumeFreeTime();
    result += "\n";

    result += "allowance: ";
    result += account.getAllowance();
    result += "\n";

    result += "latest_withdraw_time: ";
    result += new Date(account.getLatestWithdrawTime());
    result += "\n";

//    result += "code: ";
//    result += account.getCode();
//    result += "\n";
//
    result += "is_witness: ";
    result += account.getIsWitness();
    result += "\n";
//
//    result += "is_committee: ";
//    result += account.getIsCommittee();
//    result += "\n";

    result += "asset_issued_name: ";
    result += account.getAssetIssuedName().toStringUtf8();
    result += "\n";
    return result;
  }

//  public static String printAccountList(AccountList accountList) {
//    String result = "\n";
//    int i = 0;
//    for (Account account : accountList.getAccountsList()) {
//      result += "account " + i + " :::";
//      result += "\n";
//      result += "[";
//      result += "\n";
//      result += printAccount(account);
//      result += "]";
//      result += "\n";
//      result += "\n";
//      i++;
//    }
//    return result;
//  }

  public static String printWitness(Witness witness) {
    String result = "";
    result += "address: ";
    result += Wallet.encode58Check(witness.getAddress().toByteArray());
    result += "\n";
    result += "voteCount: ";
    result += witness.getVoteCount();
    result += "\n";
    result += "pubKey: ";
    result += ByteArray.toHexString(witness.getPubKey().toByteArray());
    result += "\n";
    result += "url: ";
    result += witness.getUrl();
    result += "\n";
    result += "totalProduced: ";
    result += witness.getTotalProduced();
    result += "\n";
    result += "totalMissed: ";
    result += witness.getTotalMissed();
    result += "\n";
    result += "latestBlockNum: ";
    result += witness.getLatestBlockNum();
    result += "\n";
    result += "latestSlotNum: ";
    result += witness.getLatestSlotNum();
    result += "\n";
    result += "isJobs: ";
    result += witness.getIsJobs();
    result += "\n";
    return result;
  }

  public static String printWitnessList(WitnessList witnessList) {
    String result = "\n";
    int i = 0;
    for (Witness witness : witnessList.getWitnessesList()) {
      result += "witness " + i + " :::";
      result += "\n";
      result += "[";
      result += "\n";
      result += printWitness(witness);
      result += "]";
      result += "\n";
      result += "\n";
      i++;
    }
    return result;
  }

  public static String printAssetIssue(AssetIssueContract assetIssue) {
    String result = "";
    result += "owner_address: ";
    result += Wallet.encode58Check(assetIssue.getOwnerAddress().toByteArray());
    result += "\n";
    result += "name: ";
    result += new String(assetIssue.getName().toByteArray(), Charset.forName("UTF-8"));
    result += "\n";
    result += "total_supply: ";
    result += assetIssue.getTotalSupply();
    result += "\n";
    result += "trx_num: ";
    result += assetIssue.getTrxNum();
    result += "\n";
    result += "num: ";
    result += assetIssue.getNum();
    result += "\n";
    result += "start_time: ";
    result += new Date(assetIssue.getStartTime());
    result += "\n";
    result += "end_time: ";
    result += new Date(assetIssue.getEndTime());
    result += "\n";
    result += "vote_score: ";
    result += assetIssue.getVoteScore();
    result += "\n";
    result += "description: ";
    result += new String(assetIssue.getDescription().toByteArray(), Charset.forName("UTF-8"));
    result += "\n";
    result += "url: ";
    result += new String(assetIssue.getUrl().toByteArray(), Charset.forName("UTF-8"));
    result += "\n";
    result += "free asset net limit: ";
    result += assetIssue.getFreeAssetNetLimit();
    result += "\n";
    result += "public free asset net limit: ";
    result += assetIssue.getPublicFreeAssetNetLimit();
    result += "\n";
    result += "public free asset net usage: ";
    result += assetIssue.getPublicFreeAssetNetUsage();
    result += "\n";
    result += "public latest free net time: ";
    result += assetIssue.getPublicLatestFreeNetTime();
    result += "\n";

    if (assetIssue.getFrozenSupplyCount() > 0) {
      for (FrozenSupply frozenSupply : assetIssue.getFrozenSupplyList()) {
        result += "frozen_supply";
        result += "\n";
        result += "{";
        result += "\n";
        result += "  amount: ";
        result += frozenSupply.getFrozenAmount();
        result += "\n";
        result += "  frozen_days: ";
        result += frozenSupply.getFrozenDays();
        result += "\n";
        result += "}";
        result += "\n";
      }
    }

    return result;
  }

  public static String printAssetIssueList(AssetIssueList assetIssueList) {
    String result = "\n";
    int i = 0;
    for (AssetIssueContract assetIssue : assetIssueList.getAssetIssueList()) {
      result += "assetIssue " + i + " :::";
      result += "\n";
      result += "[";
      result += "\n";
      result += printAssetIssue(assetIssue);
      result += "]";
      result += "\n";
      result += "\n";
      i++;
    }
    return result;
  }

  public static String printContract(Transaction.Contract contract) {
    String result = "";
    try {
      result += "contract_type: ";
      result += contract.getType();
      result += "\n";

      switch (contract.getType()) {
        case AccountCreateContract:
          AccountCreateContract accountCreateContract = contract.getParameter()
              .unpack(AccountCreateContract.class);
          result += "type: ";
          result += accountCreateContract.getType();
          result += "\n";
          if (accountCreateContract.getAccountAddress() != null
              && accountCreateContract.getAccountAddress().size() > 0) {
            result += "account_address: ";
            result += Wallet
                .encode58Check(accountCreateContract.getAccountAddress().toByteArray());
            result += "\n";
          }
          result += "owner_address: ";
          result += Wallet
              .encode58Check(accountCreateContract.getOwnerAddress().toByteArray());
          result += "\n";
          break;
        case AccountUpdateContract:
          AccountUpdateContract accountUpdateContract = contract.getParameter()
              .unpack(AccountUpdateContract.class);
          if (accountUpdateContract.getAccountName() != null
              && accountUpdateContract.getAccountName().size() > 0) {
            result += "account_name: ";
            result += new String(accountUpdateContract.getAccountName().toByteArray(),
                Charset.forName("UTF-8"));
            result += "\n";
          }
          result += "owner_address: ";
          result += Wallet
              .encode58Check(accountUpdateContract.getOwnerAddress().toByteArray());
          result += "\n";
          break;
        case TransferContract:
          TransferContract transferContract = contract.getParameter()
              .unpack(TransferContract.class);
          result += "owner_address: ";
          result += Wallet
              .encode58Check(transferContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "to_address: ";
          result += Wallet
              .encode58Check(transferContract.getToAddress().toByteArray());
          result += "\n";
          result += "amount: ";
          result += transferContract.getAmount();
          result += "\n";
          break;
        case TransferAssetContract:
          TransferAssetContract transferAssetContract = contract.getParameter()
              .unpack(TransferAssetContract.class);
          result += "asset_name: ";
          result += new String(transferAssetContract.getAssetName().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          result += "owner_address: ";
          result += Wallet
              .encode58Check(transferAssetContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "to_address: ";
          result += Wallet
              .encode58Check(transferAssetContract.getToAddress().toByteArray());
          result += "\n";
          result += "amount: ";
          result += transferAssetContract.getAmount();
          result += "\n";
          break;
        case VoteAssetContract:
          VoteAssetContract voteAssetContract = contract.getParameter()
              .unpack(VoteAssetContract.class);
          break;
        case VoteWitnessContract:
          VoteWitnessContract voteWitnessContract = contract.getParameter()
              .unpack(VoteWitnessContract.class);
          result += "owner_address: ";
          result += Wallet
              .encode58Check(voteWitnessContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "votes: ";
          result += "\n";
          result += "{";
          result += "\n";
          for (VoteWitnessContract.Vote vote : voteWitnessContract.getVotesList()) {
            result += "[";
            result += "\n";
            result += "vote_address: ";
            result += Wallet
                .encode58Check(vote.getVoteAddress().toByteArray());
            result += "\n";
            result += "vote_count: ";
            result += vote.getVoteCount();
            result += "\n";
            result += "]";
            result += "\n";
          }
          result += "}";
          result += "\n";
          break;
        case WitnessCreateContract:
          WitnessCreateContract witnessCreateContract = contract.getParameter()
              .unpack(WitnessCreateContract.class);
          result += "owner_address: ";
          result += Wallet
              .encode58Check(witnessCreateContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "url: ";
          result += new String(witnessCreateContract.getUrl().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          break;
        case WitnessUpdateContract:
          WitnessUpdateContract witnessUpdateContract = contract.getParameter()
              .unpack(WitnessUpdateContract.class);
          result += "owner_address: ";
          result += Wallet
              .encode58Check(witnessUpdateContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "url: ";
          result += new String(witnessUpdateContract.getUpdateUrl().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          break;
        case AssetIssueContract:
          AssetIssueContract assetIssueContract = contract.getParameter()
              .unpack(AssetIssueContract.class);
          result += printAssetIssue(assetIssueContract);
          break;
        case UpdateAssetContract:
          UpdateAssetContract updateAssetContract = contract.getParameter()
              .unpack(UpdateAssetContract.class);
          result += "owner_address: ";
          result += Wallet.encode58Check(updateAssetContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "description: ";
          result += new String(updateAssetContract.getDescription().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          result += "url: ";
          result += new String(updateAssetContract.getUrl().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          result += "free asset net limit: ";
          result += updateAssetContract.getNewLimit();
          result += "\n";
          result += "public free asset net limit: ";
          result += updateAssetContract.getNewPublicLimit();
          result += "\n";
          break;
        case ParticipateAssetIssueContract:
          ParticipateAssetIssueContract participateAssetIssueContract = contract.getParameter()
              .unpack(ParticipateAssetIssueContract.class);
          result += "asset_name: ";
          result += new String(participateAssetIssueContract.getAssetName().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          result += "owner_address: ";
          result += Wallet
              .encode58Check(participateAssetIssueContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "to_address: ";
          result += Wallet
              .encode58Check(participateAssetIssueContract.getToAddress().toByteArray());
          result += "\n";
          result += "amount: ";
          result += participateAssetIssueContract.getAmount();
          result += "\n";
          break;
        case FreezeBalanceContract:
          FreezeBalanceContract freezeBalanceContract = contract.getParameter()
              .unpack(FreezeBalanceContract.class);
          result += "owner_address: ";
          result += Wallet
              .encode58Check(freezeBalanceContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "frozen_balance: ";
          result += freezeBalanceContract.getFrozenBalance();
          result += "\n";
          result += "frozen_duration: ";
          result += freezeBalanceContract.getFrozenDuration();
          result += "\n";
          break;
        case UnfreezeBalanceContract:
          UnfreezeBalanceContract unfreezeBalanceContract = contract.getParameter()
              .unpack(UnfreezeBalanceContract.class);
          result += "owner_address: ";
          result += Wallet
              .encode58Check(unfreezeBalanceContract.getOwnerAddress().toByteArray());
          result += "\n";
          break;
        case UnfreezeAssetContract:
          UnfreezeAssetContract unfreezeAssetContract = contract.getParameter()
              .unpack(UnfreezeAssetContract.class);
          result += "owner_address: ";
          result += Wallet
              .encode58Check(unfreezeAssetContract.getOwnerAddress().toByteArray());
          result += "\n";
          break;
        case WithdrawBalanceContract:
          WithdrawBalanceContract withdrawBalanceContract = contract.getParameter()
              .unpack(WithdrawBalanceContract.class);
          result += "owner_address: ";
          result += Wallet
              .encode58Check(withdrawBalanceContract.getOwnerAddress().toByteArray());
          result += "\n";
          break;
        case DeployContract:
          DeployContract deployContract = contract.getParameter().unpack(DeployContract.class);
          result += "owner_address: ";
          result += Wallet
              .encode58Check(deployContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "script: ";
          result += new String(deployContract.getScript().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          break;
        default:
          return "";
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return "";
    }
    return result;
  }

  public static String printContractList(List<Contract> contractList) {
    String result = "";
    int i = 0;
    for (Contract contract : contractList) {
      result += "contract " + i + " :::";
      result += "\n";
      result += "[";
      result += "\n";
      result += printContract(contract);
      result += "]";
      result += "\n";
      result += "\n";
      i++;
    }
    return result;
  }

  public static String printTransactionRow(Transaction.raw raw) {
    String result = "";

    if (raw.getRefBlockBytes() != null) {
      result += "ref_block_bytes: ";
      result += ByteArray.toHexString(raw.getRefBlockBytes().toByteArray());
      result += "\n";
    }

    result += "ref_block_num: ";
    result += raw.getRefBlockNum();
    result += "\n";

    if (raw.getRefBlockHash() != null) {
      result += "ref_block_hash: ";
      result += ByteArray.toHexString(raw.getRefBlockHash().toByteArray());
      result += "\n";
    }

    if (raw.getContractCount() > 0) {
      result += "contract: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printContractList(raw.getContractList());
      result += "}";
      result += "\n";
    }

    result += "timestamp: ";
    result += new Date(raw.getTimestamp());
    result += "\n";
    return result;
  }

  public static String printSignature(List<ByteString> signatureList) {
    String result = "";
    int i = 0;
    for (ByteString signature : signatureList) {
      result += "signature " + i + " :";
      result += ByteArray.toHexString(signature.toByteArray());
      result += "\n";
      i++;
    }
    return result;
  }

  public static String printRet(List<Result> resultList) {
    String results = "";
    int i = 0;
    for (Result result : resultList) {
      results += "result: ";
      results += i;
      results += " ::: ";
      results += "\n";
      results += "[";
      results += "\n";
      results += "code ::: ";
      results += result.getRet();
      results += "\n";
      results += "fee ::: ";
      results += result.getFee();
      results += "\n";
      results += "]";
      results += "\n";
      i++;
    }
    return results;
  }

  public static String printTransaction(Transaction transaction) {
    String result = "";
    result += "hash: ";
    result += "\n";
    result += ByteArray.toHexString(Sha256Hash.hash(transaction.toByteArray()));
    result += "\n";
    result += "txid: ";
    result += "\n";
    result += ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    result += "\n";

    if (transaction.getRawData() != null) {
      result += "raw_data: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printTransactionRow(transaction.getRawData());
      result += "}";
      result += "\n";
    }
    if (transaction.getSignatureCount() > 0) {
      result += "signature: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printSignature(transaction.getSignatureList());
      result += "}";
      result += "\n";
    }
    if (transaction.getRetCount() != 0) {
      result += "ret: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printRet(transaction.getRetList());
      result += "}";
      result += "\n";
    }
    return result;
  }

  public static String printTransactionInfo(TransactionInfo transactionInfo) {
    String result = "";
    result += "txid: ";
    result += "\n";
    result += ByteArray.toHexString(transactionInfo.getId().toByteArray());
    result += "\n";
    result += "fee: ";
    result += "\n";
    result += transactionInfo.getFee();
    result += "\n";
    result += "blockNumber: ";
    result += "\n";
    result += transactionInfo.getBlockNumber();
    result += "\n";
    result += "blockTimeStamp: ";
    result += "\n";
    result += transactionInfo.getBlockTimeStamp();
    result += "\n";
    return result;
  }

  public static String printTransactionList(TransactionList transactionList) {
    return printTransactions(transactionList.getTransactionList());
  }

  public static String printTransactions(List<Transaction> transactionList) {
    String result = "\n";
    int i = 0;
    for (Transaction transaction : transactionList) {
      result += "transaction " + i + " :::";
      result += "\n";
      result += "[";
      result += "\n";
      result += printTransaction(transaction);
      result += "]";
      result += "\n";
      result += "\n";
      i++;
    }
    return result;
  }

  public static String printBlockRow(BlockHeader.raw raw) {
    String result = "";

    result += "timestamp: ";
    result += new Date(raw.getTimestamp());
    result += "\n";

    result += "txTrieRoot: ";
    result += ByteArray.toHexString(raw.getTxTrieRoot().toByteArray());
    result += "\n";

    result += "parentHash: ";
    result += ByteArray.toHexString(raw.getParentHash().toByteArray());
    result += "\n";

    result += "number: ";
    result += raw.getNumber();
    result += "\n";

    result += "witness_id: ";
    result += raw.getWitnessId();
    result += "\n";

    result += "witness_address: ";
    result += Wallet.encode58Check(raw.getWitnessAddress().toByteArray());
    result += "\n";

    return result;
  }


  public static String printBlockHeader(BlockHeader blockHeader) {
    String result = "";
    result += "raw_data: ";
    result += "\n";
    result += "{";
    result += "\n";
    result += printBlockRow(blockHeader.getRawData());
    result += "}";
    result += "\n";

    result += "witness_signature: ";
    result += "\n";
    result += ByteArray.toHexString(blockHeader.getWitnessSignature().toByteArray());
    result += "\n";
    return result;
  }

  public static String printBlock(Block block) {
    String result = "\n";
    if (block.getBlockHeader() != null) {
      result += "block_header: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printBlockHeader(block.getBlockHeader());
      result += "}";
      result += "\n";
    }
    if (block.getTransactionsCount() > 0) {
      result += printTransactions(block.getTransactionsList());
    }
    return result;
  }

  public static String printBlockList(BlockList blockList) {
    String result = "\n";
    int i = 0;
    for (Block block : blockList.getBlockList()) {
      result += "block " + i + " :::";
      result += "\n";
      result += "[";
      result += "\n";
      result += printBlock(block);
      result += "]";
      result += "\n";
      result += "\n";
      i++;
    }
    return result;
  }

  public static String printAccountNet(AccountNetMessage accountNet) {
    String result = "";
    result += "free_net_used: ";
    result += accountNet.getFreeNetUsed();
    result += "\n";
    result += "free_net_limit: ";
    result += accountNet.getFreeNetLimit();
    result += "\n";
    result += "net_used: ";
    result += accountNet.getNetUsed();
    result += "\n";
    result += "net_limit: ";
    result += accountNet.getNetLimit();
    result += "\n";
    result += "total_net_limit: ";
    result += accountNet.getTotalNetLimit();
    result += "\n";
    result += "total_net_weight: ";
    result += accountNet.getTotalNetWeight();
    result += "\n";

    if (accountNet.getAssetNetLimitCount() > 0) {
      for (String name : accountNet.getAssetNetLimitMap().keySet()) {
        result += "asset";
        result += "\n";
        result += "{";
        result += "\n";
        result += "  name: ";
        result += name;
        result += "\n";
        result += "  free_asset_net_used: ";
        result += accountNet.getAssetNetUsedMap().get(name);
        result += "\n";
        result += "  free_asset_net_limit: ";
        result += accountNet.getAssetNetLimitMap().get(name);
        result += "\n";
        result += "}";
        result += "\n";
      }
    }
    return result;
  }

  public static Transaction packTransaction(String strTransaction) {
    JSONObject jsonTransaction = JSONObject.parseObject(strTransaction);
    JSONObject rawData = jsonTransaction.getJSONObject("raw_data");
    JSONArray contracts = new JSONArray();
    JSONArray rawContractArray = rawData.getJSONArray("contract");

    for (int i = 0; i < rawContractArray.size(); i++) {
      try {
        JSONObject contract = rawContractArray.getJSONObject(i);
        JSONObject parameter = contract.getJSONObject("parameter");
        String contractType = contract.getString("type");
        Any any = null;
        switch (contractType) {
          case "AccountCreateContract":
            AccountCreateContract.Builder accountCreateContractBuilder = AccountCreateContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                accountCreateContractBuilder);
            any = Any.pack(accountCreateContractBuilder.build());
            break;
          case "TransferContract":
            TransferContract.Builder transferContractBuilder = TransferContract.newBuilder();
            JsonFormat
                .merge(parameter.getJSONObject("value").toJSONString(), transferContractBuilder);
            any = Any.pack(transferContractBuilder.build());
            break;
          case "TransferAssetContract":
            TransferAssetContract.Builder transferAssetContractBuilder = TransferAssetContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                transferAssetContractBuilder);
            any = Any.pack(transferAssetContractBuilder.build());
            break;
          case "VoteAssetContract":
            VoteAssetContract.Builder voteAssetContractBuilder = VoteAssetContract.newBuilder();
            JsonFormat
                .merge(parameter.getJSONObject("value").toJSONString(), voteAssetContractBuilder);
            any = Any.pack(voteAssetContractBuilder.build());
            break;
          case "VoteWitnessContract":
            VoteWitnessContract.Builder voteWitnessContractBuilder = VoteWitnessContract
                .newBuilder();
            JsonFormat
                .merge(parameter.getJSONObject("value").toJSONString(), voteWitnessContractBuilder);
            any = Any.pack(voteWitnessContractBuilder.build());
            break;
          case "WitnessCreateContract":
            WitnessCreateContract.Builder witnessCreateContractBuilder = WitnessCreateContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                witnessCreateContractBuilder);
            any = Any.pack(witnessCreateContractBuilder.build());
            break;
          case "AssetIssueContract":
            AssetIssueContract.Builder assetIssueContractBuilder = AssetIssueContract.newBuilder();
            JsonFormat
                .merge(parameter.getJSONObject("value").toJSONString(), assetIssueContractBuilder);
            any = Any.pack(assetIssueContractBuilder.build());
            break;
          case "DeployContract":
            DeployContract.Builder deployContractBuilder = DeployContract.newBuilder();
            JsonFormat
                .merge(parameter.getJSONObject("value").toJSONString(), deployContractBuilder);
            any = Any.pack(deployContractBuilder.build());
            break;
          case "WitnessUpdateContract":
            WitnessUpdateContract.Builder witnessUpdateContractBuilder = WitnessUpdateContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                witnessUpdateContractBuilder);
            any = Any.pack(witnessUpdateContractBuilder.build());
            break;
          case "ParticipateAssetIssueContract":
            ParticipateAssetIssueContract.Builder participateAssetIssueContractBuilder = ParticipateAssetIssueContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                participateAssetIssueContractBuilder);
            any = Any.pack(participateAssetIssueContractBuilder.build());
            break;
          case "AccountUpdateContract":
            AccountUpdateContract.Builder accountUpdateContractBuilder = AccountUpdateContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                accountUpdateContractBuilder);
            any = Any.pack(accountUpdateContractBuilder.build());
            break;
          case "FreezeBalanceContract":
            FreezeBalanceContract.Builder freezeBalanceContractBuilder = FreezeBalanceContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                freezeBalanceContractBuilder);
            any = Any.pack(freezeBalanceContractBuilder.build());
            break;
          case "UnfreezeBalanceContract":
            UnfreezeBalanceContract.Builder unfreezeBalanceContractBuilder = UnfreezeBalanceContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                unfreezeBalanceContractBuilder);
            any = Any.pack(unfreezeBalanceContractBuilder.build());
            break;
          case "UnfreezeAssetContract":
            UnfreezeAssetContract.Builder unfreezeAssetContractBuilder = UnfreezeAssetContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                unfreezeAssetContractBuilder);
            any = Any.pack(unfreezeAssetContractBuilder.build());
            break;
          case "WithdrawBalanceContract":
            WithdrawBalanceContract.Builder withdrawBalanceContractBuilder = WithdrawBalanceContract
                .newBuilder();
            JsonFormat.merge(parameter.getJSONObject("value").toJSONString(),
                withdrawBalanceContractBuilder);
            any = Any.pack(withdrawBalanceContractBuilder.build());
            break;
          case "UpdateAssetContract":
            UpdateAssetContract.Builder updateAssetContractBuilder = UpdateAssetContract
                .newBuilder();
            JsonFormat
                .merge(parameter.getJSONObject("value").toJSONString(), updateAssetContractBuilder);
            any = Any.pack(updateAssetContractBuilder.build());
            break;
          // todo add other contract
          default:
        }
        if (any != null) {
          String value = ByteArray.toHexString(any.getValue().toByteArray());
          parameter.put("value", value);
          contract.put("parameter", parameter);
          contracts.add(contract);
        }
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    rawData.put("contract", contracts);
    jsonTransaction.put("raw_data", rawData);
    Transaction.Builder transactionBuilder = Transaction.newBuilder();
    try {
      JsonFormat.merge(jsonTransaction.toJSONString(), transactionBuilder);
      return transactionBuilder.build();
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String printTransactionJOSN(Transaction transaction) {
    return printTransactionToJSON(transaction).toJSONString();
  }

  public static JSONObject printTransactionToJSON(Transaction transaction) {
    JSONObject jsonTransaction = JSONObject.parseObject(JsonFormat.printToString(transaction));
    JSONArray contracts = new JSONArray();
    transaction.getRawData().getContractList().stream().forEach(contract -> {
      try {
        JSONObject contractJson = null;
        Any contractParameter = contract.getParameter();
        switch (contract.getType()) {
          case AccountCreateContract:
            AccountCreateContract accountCreateContract = contractParameter
                .unpack(AccountCreateContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(accountCreateContract));
            break;
          case TransferContract:
            TransferContract transferContract = contractParameter.unpack(TransferContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(transferContract));
            break;
          case TransferAssetContract:
            TransferAssetContract transferAssetContract = contractParameter
                .unpack(TransferAssetContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(transferAssetContract));
            break;
          case VoteAssetContract:
            VoteAssetContract voteAssetContract = contractParameter.unpack(VoteAssetContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(voteAssetContract));
            break;
          case VoteWitnessContract:
            VoteWitnessContract voteWitnessContract = contractParameter
                .unpack(VoteWitnessContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(voteWitnessContract));
            break;
          case WitnessCreateContract:
            WitnessCreateContract witnessCreateContract = contractParameter
                .unpack(WitnessCreateContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(witnessCreateContract));
            break;
          case AssetIssueContract:
            AssetIssueContract assetIssueContract = contractParameter
                .unpack(AssetIssueContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(assetIssueContract));
            break;
          case DeployContract:
            DeployContract deployContract = contractParameter.unpack(DeployContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(deployContract));
            break;
          case WitnessUpdateContract:
            WitnessUpdateContract witnessUpdateContract = contractParameter
                .unpack(WitnessUpdateContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(witnessUpdateContract));
            break;
          case ParticipateAssetIssueContract:
            ParticipateAssetIssueContract participateAssetIssueContract = contractParameter
                .unpack(ParticipateAssetIssueContract.class);
            contractJson = JSONObject
                .parseObject(JsonFormat.printToString(participateAssetIssueContract));
            break;
          case AccountUpdateContract:
            AccountUpdateContract accountUpdateContract = contractParameter
                .unpack(AccountUpdateContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(accountUpdateContract));
            break;
          case FreezeBalanceContract:
            FreezeBalanceContract freezeBalanceContract = contractParameter
                .unpack(FreezeBalanceContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(freezeBalanceContract));
            break;
          case UnfreezeBalanceContract:
            UnfreezeBalanceContract unfreezeBalanceContract = contractParameter
                .unpack(UnfreezeBalanceContract.class);
            contractJson = JSONObject
                .parseObject(JsonFormat.printToString(unfreezeBalanceContract));
            break;
          case UnfreezeAssetContract:
            UnfreezeAssetContract unfreezeAssetContract = contractParameter
                .unpack(UnfreezeAssetContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(unfreezeAssetContract));
            break;
          case WithdrawBalanceContract:
            WithdrawBalanceContract withdrawBalanceContract = contractParameter
                .unpack(WithdrawBalanceContract.class);
            contractJson = JSONObject
                .parseObject(JsonFormat.printToString(withdrawBalanceContract));
            break;
          case UpdateAssetContract:
            UpdateAssetContract updateAssetContract = contractParameter
                .unpack(UpdateAssetContract.class);
            contractJson = JSONObject.parseObject(JsonFormat.printToString(updateAssetContract));
            break;
          // todo add other contract
          default:
        }
        JSONObject parameter = new JSONObject();
        parameter.put("value", contractJson);
        parameter.put("type_url", contract.getParameterOrBuilder().getTypeUrl());
        JSONObject jsonContract = new JSONObject();
        jsonContract.put("parameter", parameter);
        jsonContract.put("type", contract.getType());
        contracts.add(jsonContract);
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }
    });

    JSONObject rawData = JSONObject.parseObject(jsonTransaction.get("raw_data").toString());
    rawData.put("contract", contracts);
    jsonTransaction.put("raw_data", rawData);
    String txID = ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    jsonTransaction.put("txID", txID);
    return jsonTransaction;
  }

}

