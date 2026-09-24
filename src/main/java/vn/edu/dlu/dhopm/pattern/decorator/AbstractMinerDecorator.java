package vn.edu.dlu.dhopm.pattern.decorator;

import vn.edu.dlu.dhopm.model.DHOList;
import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.util.List;
import java.util.Objects;

/**
 * Base Decorator trong <b>Decorator Pattern (GoF)</b>.
 *
 * <p>Duy trì một tham chiếu đến đối tượng {@link StreamMiner} được trang trí (Component)
 * và ủy quyền mặc định tất cả các lời gọi hàm cho đối tượng đó.</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public abstract class AbstractMinerDecorator implements StreamMiner {

    protected final StreamMiner wrappedMiner;

    public AbstractMinerDecorator(StreamMiner wrappedMiner) {
        this.wrappedMiner = Objects.requireNonNull(wrappedMiner, "wrappedMiner không được phép null");
    }

    @Override
    public MiningCycleResult processBatchAndMine(List<Transaction> batch, MiningConfig config) {
        return wrappedMiner.processBatchAndMine(batch, config);
    }

    @Override
    public List<PatternResult> mine(MiningConfig config) {
        return wrappedMiner.mine(config);
    }

    @Override
    public DHOList getDHOList() {
        return wrappedMiner.getDHOList();
    }

    @Override
    public int getCurrentTL() {
        return wrappedMiner.getCurrentTL();
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return wrappedMiner.getAllTransactions();
    }

    @Override
    public void reset() {
        wrappedMiner.reset();
    }

    public StreamMiner getWrappedMiner() {
        return wrappedMiner;
    }
}
