package datastructures

func Remove[T any](slice []T, i int) []T {
	if i+1 < len(slice) {
		return append(slice[:i], slice[i+1:]...)
	}
	return slice[:i]
}
