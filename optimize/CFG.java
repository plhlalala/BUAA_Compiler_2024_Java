package optimize;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.IrModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

public class CFG {
    Function func;
    private HashMap<BasicBlock, ArrayList<BasicBlock>> preMap; // 前驱关系映射
    private HashMap<BasicBlock, ArrayList<BasicBlock>> sucMap; // 后继关系映射
    private HashMap<BasicBlock, ArrayList<BasicBlock>> domMap; // 支配集合映射
    private HashMap<BasicBlock, BasicBlock> parentMap; // 父支配关系映射
    private HashMap<BasicBlock, ArrayList<BasicBlock>> childMap; // 子支配关系映射

    public void optimize(IrModule module) {
        for (Function func : module.getFunctionListWithMain()) {
            this.func = func;
            this.preMap = new HashMap<>();
            this.sucMap = new HashMap<>();
            this.domMap = new HashMap<>();
            this.parentMap = new HashMap<>();
            this.childMap = new HashMap<>();
            // 为每个基本块初始化前驱、后继、支配集合等
            for (BasicBlock block : func.getBasicBlocks()) {
                preMap.put(block, new ArrayList<>());
                sucMap.put(block, new ArrayList<>());
                domMap.put(block, new ArrayList<>());
                parentMap.put(block, null);
                childMap.put(block, new ArrayList<>());
            }
            buildCFG();
            buildDom();
            buildImmediateDominator();
            buildDominanceFrontier();
        }
    }

    // 构建控制流图（CFG）
    private void buildCFG() {
        // 遍历函数的基本块，建立基本块之间的前驱后继关系
        for (BasicBlock block : func.getBasicBlocks()) {
            for (BasicBlock succ : block.getSuccessors()) {
                preMap.get(succ).add(block); // succ的前驱是当前block
                sucMap.get(block).add(succ); // 当前block的后继是succ
            }
        }
        // 设置每个基本块的前驱和后继
        for (BasicBlock block : func.getBasicBlocks()) {
            block.setPredecessors(preMap.get(block));
            block.setSuccessors(sucMap.get(block));
        }
        // 设置函数的前驱后继映射
        this.func.setPreMap(preMap);
        this.func.setSucMap(sucMap);
    }

    // 构建支配集合（Dom）
    private void buildDom() {
        HashMap<BasicBlock, HashSet<BasicBlock>> tmpDomMap = new HashMap<>();
        LinkedList<BasicBlock> basicBlocks = new LinkedList<>(func.getBasicBlocks());
        BasicBlock entry = basicBlocks.getFirst(); // 获取入口基本块
        // 初始化：入口基本块的支配集合是它自己，其他基本块支配集合是所有基本块
        for (BasicBlock block : basicBlocks) {
            HashSet<BasicBlock> domSet = new HashSet<>();
            if (block == entry) {
                domSet.add(block); // 入口基本块的支配集合初始化为全体基本块
            } else {
                domSet.addAll(basicBlocks); // 其他块的支配集合初始化为全体基本块
            }
            tmpDomMap.put(block, domSet);
        }
        // 固定点迭代法求解支配集合
        // 基本块A支配基本块B：从程序的入口开始，任何执行到B的路径都必须先执行A
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock block : basicBlocks) {
                if (block == entry) {
                    continue; // 入口基本块的支配集合不需要更新
                }
                if(preMap.get(block).size() == 0) {
                    continue; // 如果没有前驱，不需要更新
                }
                HashSet<BasicBlock> newDom = new HashSet<>(tmpDomMap.get(preMap.get(block).get(0)));
                // 对当前基本块的所有前驱进行交集操作
                for (int i = 1; i < preMap.get(block).size(); i++) {
                    BasicBlock pred = preMap.get(block).get(i);
                    newDom.retainAll(tmpDomMap.get(pred)); // 取交集
                }
                newDom.add(block); // 加上当前基本块自己
                // 判断支配集合是否发生变化
                if (!newDom.equals(tmpDomMap.get(block))) {
                    tmpDomMap.put(block, newDom);
                    changed = true; // 如果有变化，继续迭代
                }
            }
        }
        for (BasicBlock block : tmpDomMap.keySet()) {
            // 支配block的
            for (BasicBlock domer : tmpDomMap.get(block)) {
                this.domMap.get(domer).add(block);
            }
        }
        // 更新最终的 domMap 到类成员变量中
        for (BasicBlock block : basicBlocks) {
            block.setDomList(domMap.get(block)); // 设置基本块的支配集合
        }
        this.func.setDomMap(domMap); // 设置函数的支配集合映射
    }

    // 构建直接支配（Immediate Dominator）
    // 基本块 A 直接支配 B，意味着：
    // A 支配 B：从程序入口到达 B 的每一条路径都必须经过 A
    // 没有其他支配块：除了 A 之外，没有其他基本块 C 支配 B 且位于 A 和 B 之间
    private void buildImmediateDominator() {
        for (BasicBlock block : func.getBasicBlocks()) {
            for (BasicBlock domed : block.getDomList()) {
                // 判断domed是否被block直接支配
                if (judge(block, domed)) {
                    parentMap.put(domed, block); // domed的父支配块是block
                    childMap.get(block).add(domed); // block是domed的父支配块，添加到childMap中
                }
            }
        }
        // 设置基本块的父支配和子支配
        for (BasicBlock block : func.getBasicBlocks()) {
            block.setParaentDom(parentMap.get(block));
            block.setChildrenDom(childMap.get(block));
        }
        func.setParentMap(parentMap); // 设置函数的父支配映射
        func.setChildMap(childMap);   // 设置函数的子支配映射
    }

    // 判断domed是否被domer直接支配
    private Boolean judge(BasicBlock domer, BasicBlock domed) {
        if (!domer.getDomList().contains(domed) || domer.equals(domed)) {
            return false; // 如果domer不支配domed，或者它们是同一个块，返回false
        }
        // 如果有中间块支配了domed且不等于domer或domed，返回false
        for (BasicBlock block : domer.getDomList()) {
            if (block != domed && block != domer && block.getDomList().contains(domed)) {
                return false;
            }
        }
        return true;
    }

    // 构建支配前沿（Dominance Frontier）
    // 支配前沿 (DF) 是指一个基本块的后继中，虽然该基本块支配了它们，但它们并不在该基本块的直接控制流路径下。
    // 换句话说，支配前沿包含了所有被支配但不是直接支配的基本块。
    private void buildDominanceFrontier() {
        HashMap<BasicBlock, HashSet<BasicBlock>> tmpDfMap = new HashMap<>();
        // 初始化每个基本块的支配前沿集合为空
        for (BasicBlock block : func.getBasicBlocks()) {
            tmpDfMap.put(block, new HashSet<>());
        }
        // x 从 a 开始，沿着支配链向上（父节点）追溯。
        // 判断条件 !x.getDomList().contains(b) || x.equals(b) 表示，如果 x 不支配 b 或者 x 和 b 相等时，就将 b 加入到 x 的支配前沿集合。
        // 然后，x 指向 x 的父节点，即 x = x.getParent()，继续向上追溯，直到找到支配链的顶端。
        for (Map.Entry<BasicBlock, ArrayList<BasicBlock>> entry : sucMap.entrySet()) {
            BasicBlock a = entry.getKey();
            for (BasicBlock b : entry.getValue()) {
                BasicBlock x = a;
                // 遍历基本块的支配链，直到找到不支配b的基本块
                while (!x.getDomList().contains(b) || x.equals(b)) {
                    tmpDfMap.get(x).add(b); // 添加到支配前沿
                    x = x.getParaentDom(); // 跳到父支配块
                    if (x == null) {
                        break; // 如果没有父支配块，则停止 // 是否可以删除
                    }
                }
            }
        }
        // 设置每个基本块的支配前沿集合
        for (BasicBlock block : func.getBasicBlocks()) {
            block.setDomFrontierList(new ArrayList<>(tmpDfMap.get(block)));
        }
    }
}
