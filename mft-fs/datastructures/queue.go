package datastructures

type Queue struct {
	backingArray []interface{}
}

func NewQueue() *Queue {
	output := new(Queue)
	output.backingArray = make([]interface{}, 0)
	return output
}

func (q *Queue) IsEmpty() bool {
	return len(q.backingArray) == 0
}

func (q *Queue) Size() int {
	return len(q.backingArray)
}

func (q *Queue) Enqueue(v interface{}) {
	q.backingArray = append(q.backingArray, v)
}

func (q *Queue) Dequeue() interface{} {
	if len(q.backingArray) == 0 {
		return nil
	}
	output := q.backingArray[0]
	q.backingArray = q.backingArray[1:]
	return output
}
